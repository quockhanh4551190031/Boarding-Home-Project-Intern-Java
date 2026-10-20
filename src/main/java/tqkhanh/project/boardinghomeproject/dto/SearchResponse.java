package tqkhanh.project.boardinghomeproject.dto;

import java.util.List;

public record SearchResponse(List<PostSearchItem> posts, List<UserSearchItem> users) {
}