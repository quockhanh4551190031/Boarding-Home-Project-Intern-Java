package tqkhanh.project.boardinghomeproject.dto;

import java.math.BigDecimal;

public record PublicHouseItem(
        Long id,
        String name,
        String location,
        int roomCount,
        int availableRoomCount,
        BigDecimal minPrice,
        String thumbnail
) {
}