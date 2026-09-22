package tqkhanh.project.boardinghomeproject.dto;

import tqkhanh.project.boardinghomeproject.entity.Gender;

import java.time.LocalDate;

public record ProfileResponse(
        String email,
        String role,
        String fullName,
        String phone,
        String avatarUrl,
        Gender gender,
        LocalDate dateOfBirth,
        String address,
        String identityNumber,
        String bio,
        boolean profileCompleted
) {
}
