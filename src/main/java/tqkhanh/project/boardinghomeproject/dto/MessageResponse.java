// dto/MessageResponse.java
package tqkhanh.project.boardinghomeproject.dto;

import tqkhanh.project.boardinghomeproject.entity.MessageType;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long chatRoomId,
        Long senderId,
        String senderEmail,
        String content,
        MessageType messageType,
        boolean isRead,
        LocalDateTime createdAt
) {}