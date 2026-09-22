package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BoardingHouseRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String ward,
        @NotBlank String city,
        @NotNull BigDecimal latitude,
        @NotNull BigDecimal longitude,
        String description
) {}