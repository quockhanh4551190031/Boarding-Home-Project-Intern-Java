package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.ReviewTargetType;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.service.ReviewService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/summary")
    public ResponseEntity<Map<Long, ReviewSummaryItem>> summaries(
            @RequestParam ReviewTargetType targetType,
            @RequestParam List<Long> ids) {
        return ResponseEntity.ok(reviewService.summaries(targetType, ids));
    }

    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<ReviewListResponse> list(
            @AuthenticationPrincipal User viewer,
            @PathVariable ReviewTargetType targetType,
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.list(viewer, targetType, targetId, page, size));
    }

    @PutMapping("/{targetType}/{targetId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponse> save(
            @AuthenticationPrincipal User user,
            @PathVariable ReviewTargetType targetType,
            @PathVariable Long targetId,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.save(user, targetType, targetId, request));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        reviewService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}