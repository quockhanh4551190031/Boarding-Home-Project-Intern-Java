package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Email @Size(max = 150, message = "Email tối đa 150 ký tự") String email,
        @NotBlank @Size(max = 128, message = "Mật khẩu quá dài") String password
) {
}