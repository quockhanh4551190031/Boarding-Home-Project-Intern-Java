package tqkhanh.project.boardinghomeproject.dto;

import java.time.LocalDateTime;

public record FavoriteResponse(
        Long favoriteId,
        RoomResponse room,
        LocalDateTime savedAt
) {
}
