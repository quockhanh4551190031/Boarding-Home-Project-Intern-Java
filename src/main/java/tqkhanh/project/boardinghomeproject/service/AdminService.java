package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.*;
import tqkhanh.project.boardinghomeproject.exception.ResourceNotFoundException;
import tqkhanh.project.boardinghomeproject.repository.*;
import tqkhanh.project.boardinghomeproject.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ForumPostRepository postRepository;
    private final ReportRepository reportRepository;
    private final BoardingHouseRepository houseRepository;
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsers(Role role, String status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result = userRepository.findAll(UserSpecifications.filter(role, status, keyword), pageable);
        return result.getContent().stream().map(this::toUserResponse).toList();
    }

    @Transactional
    public AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (user.getRole() == Role.ADMIN && "BLOCKED".equals(request.status())) {
            throw new IllegalArgumentException("Không thể khóa tài khoản ADMIN");
        }

        user.setStatus(request.status());
        userRepository.save(user);
        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPosts(PostStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ForumPost> result = status != null
                ? postRepository.findByStatus(status, pageable)
                : postRepository.findAll(pageable);

        return result.getContent().stream().map(this::toPostResponse).toList();
    }

    @Transactional
    public void updatePostStatus(Long postId, UpdatePostStatusRequest request) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));
        post.setStatus(request.status());
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public List<AdminReportResponse> getReports(ReportStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> result = status != null
                ? reportRepository.findByStatus(status, pageable)
                : reportRepository.findAll(pageable);

        return result.getContent().stream().map(this::toReportResponse).toList();
    }

    @Transactional
    public AdminReportResponse updateReportStatus(Long reportId, UpdateReportStatusRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy report"));
        report.setStatus(request.status());
        reportRepository.save(report);
        return toReportResponse(report);
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long totalTenants = userRepository.count(UserSpecifications.filter(Role.TENANT, null, null));
        long totalLandlords = userRepository.count(UserSpecifications.filter(Role.LANDLORD, null, null));
        long totalAdmins = userRepository.count(UserSpecifications.filter(Role.ADMIN, null, null));

        long totalHouses = houseRepository.count();
        long totalRooms = roomRepository.count();
        long totalAvailableRooms = roomRepository.findAll().stream()
                .filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count();

        long totalPosts = postRepository.count();
        long pendingReports = reportRepository.findByStatus(ReportStatus.PENDING, Pageable.unpaged())
                .getTotalElements();

        return new AdminStatsResponse(
                totalUsers, totalTenants, totalLandlords, totalAdmins,
                totalHouses, totalRooms, totalAvailableRooms,
                totalPosts, pendingReports
        );
    }

    private AdminUserResponse toUserResponse(User user) {
        return new AdminUserResponse(
                user.getId(), user.getEmail(), user.getRole(), user.getStatus(),
                user.isProfileCompleted(), user.getCreatedAt()
        );
    }

    private PostResponse toPostResponse(ForumPost post) {
        return new PostResponse(
                post.getId(), post.getUser().getId(), post.getUser().getEmail(),
                post.getUser().getProfile() != null ? post.getUser().getProfile().getFullName() : null,
                post.getTitle(), post.getContent(),
                post.getSharedRoom() != null ? post.getSharedRoom().getId() : null,
                post.getSharedRoom() != null ? post.getSharedRoom().getTitle() : null,
                post.getLikeCount(), false, 0, post.getStatus(), post.getCreatedAt()
        );
    }

    private AdminReportResponse toReportResponse(Report report) {
        return new AdminReportResponse(
                report.getId(), report.getReporter().getEmail(),
                report.getTargetType(), report.getTargetId(), report.getReason(),
                report.getStatus(), report.getCreatedAt()
        );
    }
}