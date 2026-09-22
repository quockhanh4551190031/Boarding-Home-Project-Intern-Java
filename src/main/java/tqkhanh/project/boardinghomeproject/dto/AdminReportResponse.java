// dto/AdminReportResponse.java
package tqkhanh.project.boardinghomeproject.dto;

import tqkhanh.project.boardinghomeproject.entity.ReportStatus;
import tqkhanh.project.boardinghomeproject.entity.ReportTargetType;

import java.time.LocalDateTime;

public record AdminReportResponse(
        Long id,
        String reporterEmail,
        ReportTargetType targetType,
        Long targetId,
        String reason,
        ReportStatus status,
        LocalDateTime createdAt
) {}