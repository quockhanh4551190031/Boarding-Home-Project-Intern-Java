package tqkhanh.project.boardinghomeproject.dto;

import java.util.List;

public record ReviewListResponse(
        ReviewSummary summary,
        List<ReviewResponse> reviews,
        boolean hasMore,
        ReviewResponse myReview,
        boolean canReview,
        String cannotReviewReason
) {
}