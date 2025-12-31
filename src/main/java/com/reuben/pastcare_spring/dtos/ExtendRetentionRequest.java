package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for extending church data retention period.
 * SUPERADMIN only operation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtendRetentionRequest {

    /**
     * Number of additional days to extend retention period
     */
    @NotNull(message = "Extension days is required")
    @Min(value = 1, message = "Extension must be at least 1 day")
    private Integer extensionDays;

    /**
     * Reason for extending retention period
     */
    @NotBlank(message = "Note/reason is required")
    private String note;
}
