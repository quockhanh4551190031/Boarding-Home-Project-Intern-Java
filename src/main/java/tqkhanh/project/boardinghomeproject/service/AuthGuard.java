package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.exception.LoginFailedException;
import tqkhanh.project.boardinghomeproject.exception.TooManyRequestsException;
import tqkhanh.project.boardinghomeproject.security.RateLimiter;
import tqkhanh.project.boardinghomeproject.util.DurationText;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthGuard {

    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final RateLimiter limiter;

    @Value("${auth.login.max-failures-per-account-ip:5}")
    private int pairMax;

    @Value("${auth.login.max-failures-per-account:20}")
    private int accountMax;

    @Value("${auth.login.max-failures-per-ip:30}")
    private int ipMax;

    @Value("${auth.login.lock-minutes:15}")
    private int lockMinutes;

    @Value("${auth.register.max-per-ip-per-hour:20}")
    private int registerMax;

    private static String norm(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String pairKey(String ip, String email) {
        return "login:pair:" + ip + "|" + email;
    }

    private static String accountKey(String email) {
        return "login:account:" + email;
    }

    private static String ipKey(String ip) {
        return "login:ip:" + ip;
    }

    private static TooManyRequestsException locked(long seconds) {
        return new TooManyRequestsException(
                "Bạn đã thử đăng nhập sai quá nhiều lần. Vui lòng thử lại sau " + DurationText.of(seconds) + ".",
                seconds);
    }

    /**
     * Gọi trước khi kiểm tra mật khẩu: đang bị khóa thì từ chối luôn, kể cả khi nhập đúng.
     */
    public void checkLoginAllowed(String ip, String rawEmail) {
        String email = norm(rawEmail);
        long wait = Math.max(
                limiter.lockRemainingSeconds(pairKey(ip, email)),
                Math.max(limiter.lockRemainingSeconds(accountKey(email)), limiter.lockRemainingSeconds(ipKey(ip))));
        if (wait > 0) throw locked(wait);
    }

    /**
     * Sai email hoặc mật khẩu. Trả về ngoại lệ phù hợp để ném ra.
     */
    public RuntimeException onBadCredentials(String ip, String rawEmail) {
        Summary s = recordAll(ip, norm(rawEmail));
        if (s.lockSeconds() > 0) return locked(s.lockSeconds());

        String message = "Email hoặc mật khẩu không đúng";
        if (s.remaining() <= 2) {
            message += ". Bạn còn " + s.remaining() + " lần thử trước khi bị khóa tạm thời";
        }
        return new LoginFailedException(message);
    }

    /**
     * Thử đăng nhập vào tài khoản đã bị khóa hoặc vô hiệu: vẫn tính là một lần thử.
     */
    public void onBlockedAccount(String ip, String rawEmail) {
        recordAll(ip, norm(rawEmail));
    }

    public void onLoginSuccess(String ip, String rawEmail) {
        limiter.reset(pairKey(ip, norm(rawEmail)));
    }

    public void checkRegisterAllowed(String ip) {
        RateLimiter.Decision d = limiter.tryAcquireAll(List.of(
                new RateLimiter.Rule("register:ip:" + ip, registerMax, Duration.ofHours(1), "REGISTER")));
        if (!d.allowed()) {
            throw new TooManyRequestsException(
                    "Có quá nhiều tài khoản được tạo từ mạng của bạn. Vui lòng thử lại sau "
                            + DurationText.of(d.retryAfterSeconds()) + ".",
                    d.retryAfterSeconds());
        }
    }

    private record Summary(long lockSeconds, int remaining) {
    }

    private Summary recordAll(String ip, String email) {
        Duration lock = Duration.ofMinutes(Math.max(lockMinutes, 1));
        RateLimiter.FailureResult pair = limiter.recordFailure(pairKey(ip, email), pairMax, WINDOW, lock);
        RateLimiter.FailureResult account = limiter.recordFailure(accountKey(email), accountMax, WINDOW, lock);
        RateLimiter.FailureResult byIp = limiter.recordFailure(ipKey(ip), ipMax, WINDOW, lock);

        long lockSeconds = Math.max(pair.lockSeconds(), Math.max(account.lockSeconds(), byIp.lockSeconds()));
        int remaining = Math.min(pair.remaining(), Math.min(account.remaining(), byIp.remaining()));
        return new Summary(lockSeconds, remaining);
    }
}