package tqkhanh.project.boardinghomeproject.dto;

import java.util.List;

public record ViewingListResponse(List<ViewingResponse> items, int currentPage, int totalPages, long total,
                                  boolean hasNext) {
}