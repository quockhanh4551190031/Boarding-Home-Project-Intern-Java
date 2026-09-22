package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank String title,
        @NotBlank String content,
        Long sharedRoomId
) {
}
