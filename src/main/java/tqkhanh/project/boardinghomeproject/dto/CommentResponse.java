package tqkhanh.project.boardinghomeproject.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long userId,
        String userEmail,
        String userFullName,
        String content,
        Long parentCommentId,
        LocalDateTime createdAt
) {
}
