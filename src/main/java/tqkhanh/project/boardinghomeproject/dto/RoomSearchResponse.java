package tqkhanh.project.boardinghomeproject.dto;

import java.util.List;

public record RoomSearchResponse(
        List<RoomResponse> rooms,
        int currentPage,
        int totalPages,
        long totalElements,
        boolean hasNext
) {
}
