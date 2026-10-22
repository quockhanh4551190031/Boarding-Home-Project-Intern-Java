package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.exception.TooManyRequestsException;
import tqkhanh.project.boardinghomeproject.security.RateLimiter;
import tqkhanh.project.boardinghomeproject.util.DurationText;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatbotGuard {

    private final RateLimiter limiter;

    @Value("${chatbot.limit.per-minute:6}")
    private int perMinute;

    @Value("${chatbot.limit.guest-per-day:30}")
    private int guestPerDay;

    @Value("${chatbot.limit.user-per-day:80}")
    private int userPerDay;

    @Value("${chatbot.limit.global-per-day:300}")
    private int globalPerDay;

    /**
     * Tài khoản đăng nhập tính theo user, khách tính theo IP.
     */
    public void check(User user, String ip) {
        String who = user != null ? "u" + user.getId() : "ip:" + ip;
        int perDay = user != null ? userPerDay : guestPerDay;

        RateLimiter.Decision d = limiter.tryAcquireAll(List.of(
                new RateLimiter.Rule("chatbot:min:" + who, perMinute, Duration.ofMinutes(1), "MINUTE"),
                new RateLimiter.Rule("chatbot:day:" + who, perDay, Duration.ofDays(1), "DAY"),
                new RateLimiter.Rule("chatbot:global", globalPerDay, Duration.ofDays(1), "GLOBAL")));

        if (d.allowed()) return;

        String wait = DurationText.of(d.retryAfterSeconds());
        String message = switch (d.code()) {
            case "MINUTE" -> "Bạn gửi tin quá nhanh. Vui lòng thử lại sau " + wait + ".";
            case "DAY" -> "Bạn đã dùng hết số lượt hỏi trợ lý hôm nay. Vui lòng thử lại sau " + wait
                    + (user == null ? ", hoặc đăng nhập để có thêm lượt." : ".");
            default -> "Trợ lý AI đang có quá nhiều yêu cầu. Vui lòng thử lại sau " + wait + ".";
        };
        throw new TooManyRequestsException(message, d.retryAfterSeconds());
    }
}