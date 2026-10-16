package tqkhanh.project.boardinghomeproject.dto;

import tqkhanh.project.boardinghomeproject.entity.PostStatus;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        Long userId,
        String authorName,
        boolean mine,
        String title,
        String content,
        Long sharedRoomId,
        String sharedRoomTitle,
        int likeCount,
        boolean likedByMe,
        int commentCount,
        PostStatus status,
        LocalDateTime createdAt
) {
}