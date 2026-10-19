package tqkhanh.project.boardinghomeproject.dto;

import java.util.List;

public record SearchResponse(List<PublicHouseItem> houses, List<UserSearchItem> users) {
}