package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Recent payment details for platform admin dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentPaymentResponse {

    /**
     * Payment ID
     */
    private Long id;

    /**
     * Church ID
     */
    private Long churchId;

    /**
     * Church name
     */
    private String churchName;

    /**
     * Payment amount (GHS)
     */
    private Double amount;

    /**
     * Amount formatted for display
     */
    private String amountDisplay;

    /**
     * Payment status (success, failed, pending)
     */
    private String status;

    /**
     * Payment reference
     */
    private String reference;

    /**
     * Subscription plan name
     */
    private String planName;

    /**
     * Payment date
     */
    private LocalDateTime paidAt;

    /**
     * Payment method (card, bank_transfer, etc.)
     */
    private String paymentMethod;

    /**
     * Transaction ID from payment gateway
     */
    private String transactionId;
}
