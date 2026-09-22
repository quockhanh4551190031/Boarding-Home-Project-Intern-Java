package tqkhanh.project.boardinghomeproject.dto;

import tqkhanh.project.boardinghomeproject.entity.HouseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BoardingHouseResponse(
        Long id,
        String name,
        String address,
        String ward,
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        String description,
        HouseStatus status,
        int roomCount,
        LocalDateTime createdAt
) {
}
