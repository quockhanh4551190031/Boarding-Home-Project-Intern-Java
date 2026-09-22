// dto/UpdatePostStatusRequest.java
package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.NotNull;
import tqkhanh.project.boardinghomeproject.entity.PostStatus;

public record UpdatePostStatusRequest(@NotNull PostStatus status) {}