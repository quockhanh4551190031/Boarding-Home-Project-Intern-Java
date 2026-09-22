package tqkhanh.project.boardinghomeproject.dto;

import tqkhanh.project.boardinghomeproject.entity.ChatSenderRole;

import java.time.LocalDateTime;

public record ChatbotHistoryItem(
        ChatSenderRole role,
        String content,
        LocalDateTime createdAt
) {
}
