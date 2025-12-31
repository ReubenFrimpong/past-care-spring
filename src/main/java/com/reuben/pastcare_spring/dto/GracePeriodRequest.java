package com.reuben.pastcare_spring.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for granting or extending grace periods.
 * Used by SUPERADMIN to manage grace periods for churches.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GracePeriodRequest {

    /**
     * Church ID to grant grace period to
     */
    @NotNull(message = "Church ID is required")
    private Long churchId;

    /**
     * Number of grace period days to grant (1-30)
     */
    @NotNull(message = "Grace period days is required")
    @Min(value = 1, message = "Grace period days must be at least 1")
    @Max(value = 30, message = "Grace period days cannot exceed 30")
    private Integer gracePeriodDays;

    /**
     * Reason for granting grace period
     */
    @NotBlank(message = "Reason is required")
    private String reason;

    /**
     * Whether to extend existing grace period (true) or reset it (false)
     */
    @Builder.Default
    private Boolean extend = false;
}
