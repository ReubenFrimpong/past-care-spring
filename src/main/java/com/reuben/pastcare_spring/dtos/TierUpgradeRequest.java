package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for initiating a tier upgrade payment.
 *
 * <p>Contains all information needed to create Paystack payment
 * and track tier change in the system.
 *
 * @since 2026-01-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierUpgradeRequest {

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

    /**
     * Email address for Paystack payment
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    /**
     * Callback URL for after payment completion
     * Typically: /billing/verify-upgrade or similar
     */
    @NotBlank(message = "Callback URL is required")
    private String callbackUrl;

    /**
     * Optional reason for upgrade (user-provided or system-generated)
     * Examples: "Exceeded member limit", "User requested upgrade", etc.
     */
    private String reason;
}
