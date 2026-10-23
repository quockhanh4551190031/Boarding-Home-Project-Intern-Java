package tqkhanh.project.boardinghomeproject.dto;

import tqkhanh.project.boardinghomeproject.entity.ViewingStatus;

import java.time.LocalDateTime;

public record ViewingResponse(
        Long id,
        ViewingStatus status,
        boolean past,
        boolean conflict,
        LocalDateTime scheduledAt,
        String message,
        String contactPhone,
        String responseNote,
        LocalDateTime respondedAt,
        LocalDateTime createdAt,
        Long roomId,
        String roomTitle,
        String roomThumbnail,
        boolean roomVisible,
        Long houseId,
        String houseName,
        String address,
        Long tenantId,
        String tenantName,
        Long landlordId,
        String landlordName
) {
}