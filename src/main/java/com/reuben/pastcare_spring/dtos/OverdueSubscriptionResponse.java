package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Overdue subscription details for platform admin alerts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueSubscriptionResponse {

    /**
     * Church ID
     */
    private Long churchId;

    /**
     * Church name
     */
    private String churchName;

    /**
     * Subscription status
     */
    private String status;

    /**
     * Plan name
     */
    private String planName;

    /**
     * Amount owed (GHS)
     */
    private Double amountOwed;

    /**
     * Amount owed formatted
     */
    private String amountOwedDisplay;

    /**
     * Days overdue
     */
    private Integer daysOverdue;

    /**
     * Period end date (when payment was due)
     */
    private LocalDate periodEnd;

    /**
     * Failed payment attempts
     */
    private Integer failedPaymentAttempts;

    /**
     * Church email for contact
     */
    private String churchEmail;

    /**
     * Last payment date
     */
    private LocalDate lastPaymentDate;
}
