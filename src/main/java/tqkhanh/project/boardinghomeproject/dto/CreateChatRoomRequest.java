// dto/CreateChatRoomRequest.java
package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotNull;

public record CreateChatRoomRequest(
        @NotNull Long targetUserId,
        Long relatedRoomId
) {}