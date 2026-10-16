package tqkhanh.project.boardinghomeproject.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long userId,
        String authorName,
        boolean mine,
        String content,
        Long parentCommentId,
        LocalDateTime createdAt
) {
}