package tqkhanh.project.boardinghomeproject.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PublicProfileResponse(
        Long id,
        String displayName,
        String role,
        String avatarUrl,
        String bio,
        LocalDateTime memberSince,
        String phone,
        boolean phoneMasked,
        boolean self,
        long forumPostCount,
        int roomCount,
        int availableRoomCount,
        List<PublicHouseItem> houses
) {
}