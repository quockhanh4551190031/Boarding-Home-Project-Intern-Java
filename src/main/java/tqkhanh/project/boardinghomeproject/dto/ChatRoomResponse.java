// dto/ChatRoomResponse.java
package tqkhanh.project.boardinghomeproject.dto;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long id,
        Long otherUserId,
        String otherUserEmail,
        String otherUserFullName,
        Long relatedRoomId,
        String relatedRoomTitle,
        String lastMessage,
        LocalDateTime lastMessageAt,
        long unreadCount
) {}