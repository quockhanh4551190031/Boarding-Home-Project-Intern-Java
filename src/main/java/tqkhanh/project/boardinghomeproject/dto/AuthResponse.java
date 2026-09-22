package tqkhanh.project.boardinghomeproject.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email,
        String role
) {
}
