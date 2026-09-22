package tqkhanh.project.boardinghomeproject.dto;

import java.util.List;

public record ChatbotMessageResponse(
        String sessionId,
        String reply,
        List<RoomResponse> suggestedRooms
) {
}
