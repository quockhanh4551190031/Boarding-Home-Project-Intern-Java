package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 150, message = "Email tối đa 150 ký tự") String email,
        @NotBlank @Size(min = 6, max = 72, message = "Mật khẩu từ 6 đến 72 ký tự") String password,
        @NotBlank String role   // "TENANT" hoặc "LANDLORD"
) {
}