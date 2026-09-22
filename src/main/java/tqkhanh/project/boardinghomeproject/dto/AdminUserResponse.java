// dto/AdminUserResponse.java
package tqkhanh.project.boardinghomeproject.dto;

import tqkhanh.project.boardinghomeproject.entity.Role;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String email,
        Role role,
        String status,
        boolean profileCompleted,
        LocalDateTime createdAt
) {}