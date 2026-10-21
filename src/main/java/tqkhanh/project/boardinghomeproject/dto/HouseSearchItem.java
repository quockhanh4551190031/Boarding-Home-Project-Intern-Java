package tqkhanh.project.boardinghomeproject.dto;

import java.math.BigDecimal;

public record HouseSearchItem(
        Long id,
        String name,
        String location,
        Double latitude,
        Double longitude,
        int roomCount,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String thumbnail,
        Double distanceKm
) {
}