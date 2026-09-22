// dto/UpdateReportStatusRequest.java
package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotNull;
import tqkhanh.project.boardinghomeproject.entity.ReportStatus;

public record UpdateReportStatusRequest(@NotNull ReportStatus status) {}