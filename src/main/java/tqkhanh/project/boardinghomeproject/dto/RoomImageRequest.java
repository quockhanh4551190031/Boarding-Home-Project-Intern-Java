// dto/RoomImageRequest.java
package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotBlank;

public record RoomImageRequest(
        @NotBlank String imageUrl,
        boolean thumbnail
) {}