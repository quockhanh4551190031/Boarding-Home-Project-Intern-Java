// dto/RoomResponse.java
package tqkhanh.project.boardinghomeproject.dto;

import tqkhanh.project.boardinghomeproject.entity.RoomStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RoomResponse(
        Long id,
        Long houseId,
        String houseName,
        Long landlordId,
        Double houseLatitude,
        Double houseLongitude,
        String title,
        BigDecimal price,
        BigDecimal area,
        Integer maxOccupants,
        String description,
        RoomStatus status,
        Integer viewCount,
        List<String> amenities,
        List<RoomImageResponse> images,
        LocalDateTime createdAt,
        Double distanceKm
) {}