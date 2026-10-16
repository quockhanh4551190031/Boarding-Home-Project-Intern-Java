package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.*;
import tqkhanh.project.boardinghomeproject.exception.ForbiddenActionException;
import tqkhanh.project.boardinghomeproject.exception.ResourceNotFoundException;
import tqkhanh.project.boardinghomeproject.exception.TooManyRequestsException;
import tqkhanh.project.boardinghomeproject.repository.BoardingHouseRepository;
import tqkhanh.project.boardinghomeproject.repository.ReviewRepository;
import tqkhanh.project.boardinghomeproject.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tqkhanh.project.boardinghomeproject.util.DisplayNames;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "(?i)(https?://|www\\.|\\b[a-z0-9-]+\\.(com|net|org|vn|info|xyz|top|club|io|me)\\b)");
    private static final Pattern LONG_DIGITS = Pattern.compile("\\d{9,}");

    private final ReviewRepository reviewRepository;
    private final BoardingHouseRepository houseRepository;
    private final RoomRepository roomRepository;

    @Value("${review.rate-limit.max-per-hour:10}")
    private int maxPerHour;

    @Value("${review.min-account-age-minutes:5}")
    private int minAccountAgeMinutes;

    private record Target(Long ownerId) {
    }

    /* ---------- Đọc ---------- */

    @Transactional(readOnly = true)
    public ReviewListResponse list(User viewer, ReviewTargetType type, Long targetId, int page, int size) {
        Target target = resolveTarget(type, targetId);

        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Page<Review> result = reviewRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDescIdDesc(
                type, targetId, PageRequest.of(Math.max(page, 0), safeSize));

        List<ReviewResponse> items = result.getContent().stream().map(r -> toResponse(r, viewer)).toList();

        ReviewResponse mine = null;
        boolean canReview = false;
        String reason = null;
        if (viewer != null) {
            mine = reviewRepository.findByUserIdAndTargetTypeAndTargetId(viewer.getId(), type, targetId)
                    .map(r -> toResponse(r, viewer))
                    .orElse(null);
            if (target.ownerId().equals(viewer.getId())) {
                reason = "Bạn không thể đánh giá " + noun(type) + " của chính mình";
            } else {
                canReview = true;
            }
        }

        return new ReviewListResponse(buildSummary(type, targetId), items, result.hasNext(), mine, canReview, reason);
    }

    @Transactional(readOnly = true)
    public Map<Long, ReviewSummaryItem> summaries(ReviewTargetType type, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();

        List<Long> limited = ids.stream().distinct().limit(100).toList();
        Map<Long, ReviewSummaryItem> result = new HashMap<>();
        for (Object[] row : reviewRepository.summarize(type, limited)) {
            long id = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            double avg = ((Number) row[2]).doubleValue();
            result.put(id, new ReviewSummaryItem(round1(avg), count));
        }
        return result;
    }

    /* ---------- Ghi ---------- */

    @Transactional
    public ReviewResponse save(User user, ReviewTargetType type, Long targetId, ReviewRequest request) {
        Target target = resolveTarget(type, targetId);

        if (target.ownerId().equals(user.getId())) {
            throw new ForbiddenActionException("Bạn không thể đánh giá " + noun(type) + " của chính mình");
        }

        LocalDateTime now = LocalDateTime.now();

        // Chống spam 1: tài khoản vừa tạo
        if (minAccountAgeMinutes > 0 && user.getCreatedAt() != null) {
            LocalDateTime allowedFrom = user.getCreatedAt().plusMinutes(minAccountAgeMinutes);
            if (now.isBefore(allowedFrom)) {
                long wait = Duration.between(now, allowedFrom).toMinutes() + 1;
                throw new ForbiddenActionException(
                        "Tài khoản mới cần chờ thêm khoảng " + wait + " phút trước khi gửi đánh giá");
            }
        }

        // Chống spam 2: nội dung quảng cáo và nội dung sao chép
        String comment = normalize(request.comment());
        if (comment != null) {
            if (containsContactInfo(comment)) {
                throw new IllegalArgumentException("Nhận xét không được chứa đường dẫn hoặc số điện thoại");
            }
            if (reviewRepository.existsDuplicateComment(
                    user.getId(), comment.toLowerCase(Locale.ROOT), type, targetId, now.minusHours(24))) {
                throw new IllegalArgumentException(
                        "Bạn vừa đăng một nhận xét giống hệt cho nơi khác. Hãy viết nhận xét riêng cho từng nơi");
            }
        }

        Optional<Review> existing =
                reviewRepository.findByUserIdAndTargetTypeAndTargetId(user.getId(), type, targetId);

        // Chống spam 3: giới hạn tần suất. Sửa lại đánh giá đã tính trong giờ qua thì không tính thêm
        if (maxPerHour > 0) {
            LocalDateTime since = now.minusHours(1);
            boolean alreadyCounted = existing.isPresent()
                    && existing.get().getUpdatedAt() != null
                    && existing.get().getUpdatedAt().isAfter(since);
            long recent = reviewRepository.countByUserIdAndUpdatedAtAfter(user.getId(), since);
            if (recent >= maxPerHour && !alreadyCounted) {
                throw new TooManyRequestsException(
                        "Bạn đã gửi quá nhiều đánh giá trong 1 giờ qua (tối đa " + maxPerHour + "). Vui lòng thử lại sau");
            }
        }

        Review review = existing.orElseGet(() -> Review.builder()
                .user(user)
                .targetType(type)
                .targetId(targetId)
                .build());
        review.setRating(request.rating());
        review.setComment(comment);

        try {
            review = reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException e) {
            // Hai yêu cầu gửi cùng lúc: ràng buộc unique ở DB chặn bản thứ hai
            throw new IllegalArgumentException("Bạn đã đánh giá mục này rồi. Hãy tải lại trang để sửa đánh giá");
        }

        return toResponse(review, user);
    }

    @Transactional
    public void delete(User user, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));

        boolean owner = review.getUser().getId().equals(user.getId());
        boolean admin = user.getRole() == Role.ADMIN;
        if (!owner && !admin) {
            throw new ForbiddenActionException("Bạn không có quyền xóa đánh giá này");
        }
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<AdminReviewResponse> adminList(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        List<Review> content = reviewRepository.findAll(
                PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt", "id"))
        ).getContent();

        Set<Long> houseIds = content.stream()
                .filter(r -> r.getTargetType() == ReviewTargetType.HOUSE)
                .map(Review::getTargetId).collect(Collectors.toSet());
        Set<Long> roomIds = content.stream()
                .filter(r -> r.getTargetType() == ReviewTargetType.ROOM)
                .map(Review::getTargetId).collect(Collectors.toSet());

        Map<Long, String> houseNames = houseRepository.findAllById(houseIds).stream()
                .collect(Collectors.toMap(BoardingHouse::getId, BoardingHouse::getName));
        Map<Long, String> roomTitles = roomRepository.findAllById(roomIds).stream()
                .collect(Collectors.toMap(Room::getId, Room::getTitle));

        return content.stream().map(r -> {
            Map<Long, String> names = r.getTargetType() == ReviewTargetType.HOUSE ? houseNames : roomTitles;
            return new AdminReviewResponse(
                    r.getId(), r.getTargetType(), r.getTargetId(),
                    names.getOrDefault(r.getTargetId(), "(đã xóa)"),
                    r.getRating(), r.getComment(), r.getUser().getEmail(), r.getCreatedAt());
        }).toList();
    }

    /* ---------- Hỗ trợ ---------- */

    private Target resolveTarget(ReviewTargetType type, Long id) {
        if (type == ReviewTargetType.HOUSE) {
            BoardingHouse house = houseRepository.findById(id)
                    .filter(h -> h.getStatus() == HouseStatus.ACTIVE)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà trọ"));
            return new Target(house.getLandlord().getId());
        }
        Room room = roomRepository.findById(id)
                .filter(r -> r.getStatus() != RoomStatus.HIDDEN
                        && r.getHouse().getStatus() == HouseStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng trọ"));
        return new Target(room.getHouse().getLandlord().getId());
    }

    private ReviewSummary buildSummary(ReviewTargetType type, Long targetId) {
        long[] dist = new long[5];
        for (Object[] row : reviewRepository.countByRating(type, targetId)) {
            int star = ((Number) row[0]).intValue();
            if (star >= 1 && star <= 5) dist[star - 1] = ((Number) row[1]).longValue();
        }
        long total = 0;
        long weighted = 0;
        for (int i = 0; i < 5; i++) {
            total += dist[i];
            weighted += dist[i] * (i + 1);
        }
        double average = total == 0 ? 0 : round1((double) weighted / total);
        return new ReviewSummary(average, total, Arrays.stream(dist).boxed().toList());
    }

    private ReviewResponse toResponse(Review r, User viewer) {
        boolean mine = viewer != null && r.getUser().getId().equals(viewer.getId());
        boolean edited = r.getUpdatedAt() != null && r.getCreatedAt() != null
                && r.getUpdatedAt().isAfter(r.getCreatedAt().plusSeconds(1));
        return new ReviewResponse(r.getId(), r.getUser().getId(), r.getRating(), r.getComment(),
                DisplayNames.of(r.getUser()), mine, edited, r.getCreatedAt());
    }

    private String normalize(String raw) {
        if (raw == null) return null;
        String text = raw.replace("\r\n", "\n").strip().replaceAll("\n{3,}", "\n\n");
        return text.isEmpty() ? null : text;
    }

    private boolean containsContactInfo(String text) {
        if (LINK_PATTERN.matcher(text).find()) return true;
        // Gộp các chữ số bị tách bằng dấu cách, chấm, gạch ngang (0912 345 678, 0912.345.678)
        String squeezed = text.replaceAll("(?<=\\d)[\\s.\\-](?=\\d)", "");
        return LONG_DIGITS.matcher(squeezed).find();
    }

    private String noun(ReviewTargetType type) {
        return type == ReviewTargetType.HOUSE ? "nhà trọ" : "phòng";
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}