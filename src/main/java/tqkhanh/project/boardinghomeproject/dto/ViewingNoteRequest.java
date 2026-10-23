package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.Size;

public record ViewingNoteRequest(@Size(max = 300, message = "Lý do tối đa 300 ký tự") String note) {
}