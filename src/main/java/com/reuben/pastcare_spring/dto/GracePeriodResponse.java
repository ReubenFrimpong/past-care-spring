package com.reuben.pastcare_spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for grace period operations.
 * Contains grace period status and details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GracePeriodResponse {

    /**
     * Church ID
     */
    private Long churchId;

    /**
     * Church name
     */
    private String churchName;

    /**
     * Current subscription status
     */
    private String subscriptionStatus;

    /**
     * Number of grace period days granted
     */
    private Integer gracePeriodDays;

    /**
     * Whether church is currently in grace period
     */
    private Boolean inGracePeriod;

    /**
     * Grace period end date (calculated from next_billing_date + grace_period_days)
     */
    private LocalDate gracePeriodEndDate;

    /**
     * Days remaining in grace period (0 if not in grace period)
     */
    private Long daysRemainingInGracePeriod;

    /**
     * Next billing date
     */
    private LocalDate nextBillingDate;

    /**
     * Reason for grace period (if available)
     */
    private String gracePeriodReason;

    /**
     * When grace period was granted/updated
     */
    private LocalDateTime updatedAt;

    /**
     * Success message
     */
    private String message;
}
