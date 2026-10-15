package tqkhanh.project.boardinghomeproject.dto;

import tqkhanh.project.boardinghomeproject.entity.ReviewTargetType;

import java.time.LocalDateTime;

public record AdminReviewResponse(
        Long id,
        ReviewTargetType targetType,
        Long targetId,
        String targetName,
        int rating,
        String comment,
        String authorEmail,
        LocalDateTime createdAt
) {
}