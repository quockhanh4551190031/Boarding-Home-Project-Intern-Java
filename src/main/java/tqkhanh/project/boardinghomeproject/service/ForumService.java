package tqkhanh.project.boardinghomeproject.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.*;
import tqkhanh.project.boardinghomeproject.exception.ForbiddenActionException;
import tqkhanh.project.boardinghomeproject.exception.ResourceNotFoundException;
import tqkhanh.project.boardinghomeproject.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumService {
    private final ForumPostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final ReportRepository reportRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public PostResponse createPost(User user, CreatePostRequest request) {
        Room sharedRoom = request.sharedRoomId() != null
                ? roomRepository.findById(request.sharedRoomId()).orElse(null)
                : null;

        ForumPost post = ForumPost.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .sharedRoom(sharedRoom)
                .build();

        postRepository.save(post);
        return toResponse(post, user.getId());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPosts(Long currentUserId, Long sharedRoomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ForumPost> posts = sharedRoomId != null
                ? postRepository.findBySharedRoomIdAndStatus(sharedRoomId, PostStatus.ACTIVE, pageable)
                : postRepository.findByStatus(PostStatus.ACTIVE, pageable);

        return posts.getContent().stream().map(p -> toResponse(p, currentUserId)).toList();
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, Long currentUserId) {
        ForumPost post = postRepository.findById(postId)
                .filter(p -> p.getStatus() == PostStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));
        return toResponse(post, currentUserId);
    }

    @Transactional
    public PostResponse updatePost(User user, Long postId, CreatePostRequest request) {
        ForumPost post = getOwnedPostOrThrow(user.getId(), postId);
        post.setTitle(request.title());
        post.setContent(request.content());
        postRepository.save(post);
        return toResponse(post, user.getId());
    }

    @Transactional
    public void deletePost(User user, Long postId) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));

        boolean isOwner = post.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ForbiddenActionException("Bạn không có quyền xóa bài viết này");
        }

        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }

    @Transactional
    public boolean toggleLike(User user, Long postId) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));

        boolean alreadyLiked = postLikeRepository.existsByPostIdAndUserId(postId, user.getId());

        if (alreadyLiked) {
            postLikeRepository.deleteByPostIdAndUserId(postId, user.getId());
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        } else {
            postLikeRepository.save(new PostLike(postId, user.getId()));
            post.setLikeCount(post.getLikeCount() + 1);
        }

        postRepository.save(post);
        return !alreadyLiked; // trả về trạng thái mới: true = vừa like, false = vừa unlike
    }

    @Transactional
    public CommentResponse addComment(User user, Long postId, CommentRequest request) {
        ForumPost post = postRepository.findById(postId)
                .filter(p -> p.getStatus() == PostStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));

        Comment parent = request.parentCommentId() != null
                ? commentRepository.findById(request.parentCommentId()).orElse(null)
                : null;

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(request.content())
                .parentComment(parent)
                .build();

        commentRepository.save(comment);
        return toCommentResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Transactional
    public void deleteComment(User user, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận"));

        boolean isOwner = comment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ForbiddenActionException("Bạn không có quyền xóa bình luận này");
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public void createReport(User reporter, ReportRequest request) {
        Report report = Report.builder()
                .reporter(reporter)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .build();
        reportRepository.save(report);
    }

    private ForumPost getOwnedPostOrThrow(Long userId, Long postId) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));
        if (!post.getUser().getId().equals(userId)) {
            throw new ForbiddenActionException("Bạn không có quyền sửa bài viết này");
        }
        return post;
    }

    private PostResponse toResponse(ForumPost post, Long currentUserId) {
        boolean likedByMe = currentUserId != null
                && postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        int commentCount = commentRepository.findByPostIdOrderByCreatedAtAsc(post.getId()).size();

        return new PostResponse(
                post.getId(), post.getUser().getId(), post.getUser().getEmail(),
                post.getUser().getProfile() != null ? post.getUser().getProfile().getFullName() : null,
                post.getTitle(), post.getContent(),
                post.getSharedRoom() != null ? post.getSharedRoom().getId() : null,
                post.getSharedRoom() != null ? post.getSharedRoom().getTitle() : null,
                post.getLikeCount(), likedByMe, commentCount, post.getStatus(), post.getCreatedAt()
        );
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(), comment.getUser().getId(), comment.getUser().getEmail(),
                comment.getUser().getProfile() != null ? comment.getUser().getProfile().getFullName() : null,
                comment.getContent(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getCreatedAt()
        );
    }
}
