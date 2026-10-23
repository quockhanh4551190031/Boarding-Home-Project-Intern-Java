package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateViewingRequest(
        @NotNull(message = "Vui lòng chọn phòng") Long roomId,
        @NotNull(message = "Vui lòng chọn ngày giờ xem phòng") LocalDateTime scheduledAt,
        @Size(max = 500, message = "Ghi chú tối đa 500 ký tự") String message,
        @Size(max = 20, message = "Số điện thoại không hợp lệ") String contactPhone
) {
}