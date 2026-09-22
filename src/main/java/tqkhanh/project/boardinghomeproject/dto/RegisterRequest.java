// RegisterRequest.java
package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tqkhanh.project.boardinghomeproject.entity.Role;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự") String password,
        @NotBlank String role   // "TENANT" hoặc "LANDLORD"
) {}