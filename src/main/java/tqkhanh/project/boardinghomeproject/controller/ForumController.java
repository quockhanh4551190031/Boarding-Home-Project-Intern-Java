package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.service.ForumService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    @PostMapping("/posts")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(forumService.createPost(user, request));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostResponse>> getPosts(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long sharedRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = user != null ? user.getId() : null;
        return ResponseEntity.ok(forumService.getPosts(currentUserId, sharedRoomId, page, size));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponse> getPostById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        Long currentUserId = user != null ? user.getId() : null;
        return ResponseEntity.ok(forumService.getPostById(id, currentUserId));
    }

    @PutMapping("/posts/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PostResponse> updatePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(forumService.updatePost(user, id, request));
    }

    @DeleteMapping("/posts/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deletePost(@AuthenticationPrincipal User user, @PathVariable Long id) {
        forumService.deletePost(user, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{id}/like")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Boolean>> toggleLike(
            @AuthenticationPrincipal User user, @PathVariable Long id) {
        boolean liked = forumService.toggleLike(user, id);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @PostMapping("/posts/{id}/comments")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CommentResponse> addComment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(forumService.addComment(user, id, request));
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(forumService.getComments(id));
    }

    @DeleteMapping("/comments/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal User user, @PathVariable Long id) {
        forumService.deleteComment(user, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/report")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> createReport(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReportRequest request) {
        forumService.createReport(user, request);
        return ResponseEntity.noContent().build();
    }
}