package tqkhanh.project.boardinghomeproject.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimiter {

    public record Rule(String key, int max, Duration window, String code) {
    }

    public record Decision(boolean allowed, long retryAfterSeconds, String code) {
        static Decision ok() {
            return new Decision(true, 0, null);
        }
    }

    public record FailureResult(boolean locked, long lockSeconds, int remaining) {
    }

    private static final class Entry {
        final ArrayDeque<Long> times = new ArrayDeque<>();
        volatile long lockedUntil;
        long expiresAt;
    }

    private static final long CLEANUP_INTERVAL_MS = 60_000;

    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanup = new AtomicLong();

    protected long now() {
        return System.currentTimeMillis();
    }

    /* ---------- Giới hạn số yêu cầu (cửa sổ trượt) ---------- */

    /**
     * Kiểm tra cả bộ quy tắc; chỉ khi tất cả cùng cho phép mới ghi nhận lượt này.
     */
    public Decision tryAcquireAll(List<Rule> rules) {
        long now = now();
        maybeCleanup(now);

        Decision worst = Decision.ok();
        for (Rule rule : rules) {
            if (rule.max() <= 0) continue; // 0 nghĩa là tắt quy tắc này
            Decision d = probe(rule, now);
            if (!d.allowed() && (worst.allowed() || d.retryAfterSeconds() > worst.retryAfterSeconds())) {
                worst = d;
            }
        }
        if (!worst.allowed()) return worst;

        for (Rule rule : rules) {
            if (rule.max() > 0) hit(rule, now);
        }
        return worst;
    }

    private Decision probe(Rule rule, long now) {
        long windowMs = rule.window().toMillis();
        Decision[] out = new Decision[1];

        entries.compute(rule.key(), (k, entry) -> {
            if (entry == null) {
                out[0] = Decision.ok();
                return null;
            }
            prune(entry, now, windowMs);
            if (entry.times.size() >= rule.max()) {
                // Thời điểm sớm nhất mà số lượt trong cửa sổ xuống dưới mức tối đa
                int excess = entry.times.size() - rule.max();
                Iterator<Long> it = entry.times.iterator();
                long pivot = 0;
                for (int i = 0; i <= excess; i++) pivot = it.next();
                out[0] = new Decision(false, ceilSeconds(pivot + windowMs - now), rule.code());
            } else {
                out[0] = Decision.ok();
            }
            return entry;
        });
        return out[0];
    }

    private void hit(Rule rule, long now) {
        long windowMs = rule.window().toMillis();
        entries.compute(rule.key(), (k, entry) -> {
            Entry e = entry == null ? new Entry() : entry;
            prune(e, now, windowMs);
            e.times.addLast(now);
            e.expiresAt = Math.max(e.expiresAt, now + windowMs);
            return e;
        });
    }

    /* ---------- Giới hạn lần thất bại, có khóa ---------- */

    public long lockRemainingSeconds(String key) {
        Entry entry = entries.get(key);
        if (entry == null) return 0;
        long left = entry.lockedUntil - now();
        return left > 0 ? ceilSeconds(left) : 0;
    }

    /**
     * Ghi nhận một lần thất bại. Đủ maxFailures trong cửa sổ thì khóa trong lockFor.
     */
    public FailureResult recordFailure(String key, int maxFailures, Duration window, Duration lockFor) {
        if (maxFailures <= 0) return new FailureResult(false, 0, Integer.MAX_VALUE);

        long now = now();
        maybeCleanup(now);
        long windowMs = window.toMillis();
        FailureResult[] out = new FailureResult[1];

        entries.compute(key, (k, entry) -> {
            Entry e = entry == null ? new Entry() : entry;

            if (e.lockedUntil > now) {
                // Đang bị khóa: không kéo dài thêm
                out[0] = new FailureResult(true, ceilSeconds(e.lockedUntil - now), 0);
                return e;
            }

            prune(e, now, windowMs);
            e.times.addLast(now);
            int count = e.times.size();

            if (count >= maxFailures) {
                e.lockedUntil = now + lockFor.toMillis();
                e.times.clear();
                e.expiresAt = e.lockedUntil;
                out[0] = new FailureResult(true, ceilSeconds(lockFor.toMillis()), 0);
            } else {
                e.expiresAt = Math.max(e.expiresAt, now + windowMs);
                out[0] = new FailureResult(false, 0, maxFailures - count);
            }
            return e;
        });
        return out[0];
    }

    public void reset(String key) {
        entries.remove(key);
    }

    /* ---------- Hỗ trợ ---------- */

    private static void prune(Entry entry, long now, long windowMs) {
        while (!entry.times.isEmpty() && entry.times.peekFirst() <= now - windowMs) {
            entry.times.pollFirst();
        }
    }

    private static long ceilSeconds(long millis) {
        return Math.max(1, (millis + 999) / 1000);
    }

    private void maybeCleanup(long now) {
        long last = lastCleanup.get();
        if (now - last < CLEANUP_INTERVAL_MS || !lastCleanup.compareAndSet(last, now)) return;

        for (String key : entries.keySet()) {
            entries.computeIfPresent(key, (k, e) -> now >= e.expiresAt && e.lockedUntil <= now ? null : e);
        }
    }
}