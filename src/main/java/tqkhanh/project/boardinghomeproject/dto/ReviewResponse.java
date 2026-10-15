// ReviewResponse.java
package tqkhanh.project.boardinghomeproject.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        int rating,
        String comment,
        String authorName,
        boolean mine,
        boolean edited,
        LocalDateTime createdAt
) {}