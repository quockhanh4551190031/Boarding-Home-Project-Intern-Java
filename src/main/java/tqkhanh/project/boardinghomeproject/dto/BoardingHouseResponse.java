package tqkhanh.project.boardinghomeproject.dto;

import tqkhanh.project.boardinghomeproject.entity.HouseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
        List<String> images,
        LocalDateTime createdAt
) {}