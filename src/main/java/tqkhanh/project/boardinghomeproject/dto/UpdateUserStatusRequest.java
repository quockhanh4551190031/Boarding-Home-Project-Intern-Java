// dto/UpdateUserStatusRequest.java
package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.Pattern;

public record UpdateUserStatusRequest(
        @Pattern(regexp = "ACTIVE|BLOCKED", message = "Status chỉ nhận ACTIVE hoặc BLOCKED") String status
) {}