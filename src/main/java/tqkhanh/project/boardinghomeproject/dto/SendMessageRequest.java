// dto/SendMessageRequest.java
package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tqkhanh.project.boardinghomeproject.entity.MessageType;

public record SendMessageRequest(
        @NotNull Long chatRoomId,
        @NotBlank String content,
        MessageType messageType
) {}