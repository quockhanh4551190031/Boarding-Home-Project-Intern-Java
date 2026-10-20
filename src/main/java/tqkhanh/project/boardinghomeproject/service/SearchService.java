package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.dto.PostSearchItem;
import tqkhanh.project.boardinghomeproject.dto.SearchResponse;
import tqkhanh.project.boardinghomeproject.dto.UserSearchItem;
import tqkhanh.project.boardinghomeproject.entity.ForumPost;
import tqkhanh.project.boardinghomeproject.entity.PostStatus;
import tqkhanh.project.boardinghomeproject.entity.Role;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.repository.CommentRepository;
import tqkhanh.project.boardinghomeproject.repository.ForumPostRepository;
import tqkhanh.project.boardinghomeproject.repository.ProfileRepository;
import tqkhanh.project.boardinghomeproject.util.DisplayNames;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 50;
    private static final int MAX_LIMIT = 20;
    private static final int SNIPPET_LENGTH = 140;

    private final ForumPostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public SearchResponse search(User viewer, String rawQuery, int limit) {
        String q = "";
        if (q.length() > MAX_LENGTH) q = q.substring(0, MAX_LENGTH).trim();
        else {
            q = rawQuery == null ? "" : rawQuery.trim().replaceAll("\\s+", " ");
        }
        if (q.length() < MIN_LENGTH) return new SearchResponse(List.of(), List.of());

        Pageable page = PageRequest.of(0, Math.min(Math.max(limit, 1), MAX_LIMIT));
        String escaped = escapeLike(q.toLowerCase(Locale.ROOT));
        String pattern = "%" + escaped + "%";
        String prefix = escaped + "%";

        String finalQ = q;
        List<PostSearchItem> posts = postRepository.search(PostStatus.ACTIVE, pattern, prefix, page).stream()
                .map(p -> toItem(p, finalQ))
                .toList();

        // Khách chưa đăng nhập chỉ tìm được chủ trọ; không công khai danh sách người thuê
        List<Role> roles = viewer == null ? List.of(Role.LANDLORD) : List.of(Role.TENANT, Role.LANDLORD);
        List<UserSearchItem> users = profileRepository.searchByName(roles, pattern, prefix, page).stream()
                .map(p -> new UserSearchItem(
                        p.getUser().getId(), p.getFullName().trim(), p.getUser().getRole().name()))
                .toList();

        return new SearchResponse(posts, users);
    }

    private PostSearchItem toItem(ForumPost p, String query) {
        return new PostSearchItem(
                p.getId(),
                p.getTitle(),
                snippet(p.getContent(), query),
                p.getUser().getId(),
                DisplayNames.of(p.getUser()),
                p.getLikeCount() == null ? 0 : p.getLikeCount(),
                commentRepository.countByPostId(p.getId()),
                p.getCreatedAt());
    }

    /**
     * Cắt đoạn nội dung quanh chỗ khớp đầu tiên, tránh cắt giữa từ.
     */
    private static String snippet(String content, String query) {
        if (content == null) return "";
        String text = content.replaceAll("\\s+", " ").trim();
        if (text.length() <= SNIPPET_LENGTH) return text;

        int idx = text.toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
        int start = idx > SNIPPET_LENGTH / 3 ? idx - SNIPPET_LENGTH / 3 : 0;
        start = Math.min(Math.max(start, 0), text.length() - 1);
        if (start > 0) {
            int space = text.indexOf(' ', start);
            if (space != -1 && (idx < 0 || space < idx)) start = space + 1;
        }

        int end = Math.min(text.length(), start + SNIPPET_LENGTH);
        if (end < text.length()) {
            int space = text.lastIndexOf(' ', end);
            if (space > start + SNIPPET_LENGTH / 2) end = space;
        }

        return (start > 0 ? "…" : "") + text.substring(start, end).trim() + (end < text.length() ? "…" : "");
    }

    /**
     * Dùng '!' làm ký tự thoát để '%' và '_' do người dùng gõ không bị hiểu là ký tự đại diện.
     */
    private static String escapeLike(String s) {
        return s.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}