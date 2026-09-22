// dto/RoomRequest.java
package tqkhanh.project.boardinghomeproject.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record RoomRequest(
        @NotNull Long houseId,
        @NotBlank String title,
        @NotNull @Positive BigDecimal price,
        @NotNull @Positive BigDecimal area,
        @Min(1) Integer maxOccupants,
        String description,
        List<Long> amenityIds
) {}