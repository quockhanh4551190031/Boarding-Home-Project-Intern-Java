package tqkhanh.project.boardinghomeproject.dto;

import java.util.List;

public record HouseSearchResponse(
        List<HouseSearchItem> houses,
        int currentPage,
        int totalPages,
        long totalHouses,
        long totalRooms,
        boolean hasNext
) {
}