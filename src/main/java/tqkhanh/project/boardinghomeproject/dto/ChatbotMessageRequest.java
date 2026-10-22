package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatbotMessageRequest(
        @Size(max = 36, message = "Mã phiên không hợp lệ") String sessionId,
        @NotBlank(message = "Tin nhắn không được để trống")
        @Size(max = 500, message = "Tin nhắn tối đa 500 ký tự") String message
) {
}