package tqkhanh.project.boardinghomeproject.dto;

public record ViewingRoomState(boolean canRequest, String reason, ViewingResponse active) {
}