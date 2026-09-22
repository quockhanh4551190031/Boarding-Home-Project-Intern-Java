package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import tqkhanh.project.boardinghomeproject.entity.Gender;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        @NotBlank(message = "Họ và tên là bắt buộc!") String fullName,
        @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không hợp lệ") String phone,

        Gender gender,
        LocalDate dateOfBirth,
        String address,
        String identityNumber,
        String bio
) {}
