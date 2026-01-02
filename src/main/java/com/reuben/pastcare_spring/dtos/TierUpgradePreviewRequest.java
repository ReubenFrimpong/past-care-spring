package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for previewing a tier upgrade calculation.
 *
 * <p>Used to get proration details before initiating payment.
 *
 * @since 2026-01-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierUpgradePreviewRequest {

    /**
     * Church ID performing the upgrade
     */
    @NotNull(message = "Church ID is required")
    private Long churchId;

    /**
     * Target tier ID to upgrade to
     */
    @NotNull(message = "New tier ID is required")
    private Long newTierId;

    /**
     * Target billing interval ID (optional for tier-only upgrade)
     * If null, keeps current billing interval
     */
    private Long newIntervalId;
}
