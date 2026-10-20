package tqkhanh.project.boardinghomeproject.dto;

import java.time.LocalDateTime;

public record PostSearchItem(
        Long id,
        String title,
        String snippet,
        Long authorId,
        String authorName,
        int likeCount,
        long commentCount,
        LocalDateTime createdAt
) {
}