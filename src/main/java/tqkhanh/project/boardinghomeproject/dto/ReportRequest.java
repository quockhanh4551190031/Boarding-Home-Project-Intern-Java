package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tqkhanh.project.boardinghomeproject.entity.ReportTargetType;

public record ReportRequest(
        @NotNull ReportTargetType targetType,
        @NotNull Long targetId,
        @NotBlank String reason
        ) {
}
