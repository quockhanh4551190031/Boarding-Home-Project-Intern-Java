package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatbotMessageRequest (
        String sessionId,
        @NotBlank String message
){
}
