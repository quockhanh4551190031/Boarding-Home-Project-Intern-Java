package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotNull;
import tqkhanh.project.boardinghomeproject.entity.RoomStatus;

public record RoomStatusRequest(@NotNull RoomStatus status) {}