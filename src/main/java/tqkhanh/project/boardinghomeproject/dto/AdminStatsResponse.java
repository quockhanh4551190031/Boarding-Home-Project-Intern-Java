// dto/AdminStatsResponse.java
package tqkhanh.project.boardinghomeproject.dto;

public record AdminStatsResponse(
        long totalUsers,
        long totalTenants,
        long totalLandlords,
        long totalAdmins,
        long totalBoardingHouses,
        long totalRooms,
        long totalAvailableRooms,
        long totalForumPosts,
        long pendingReports
) {}