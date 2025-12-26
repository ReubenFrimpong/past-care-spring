package com.reuben.pastcare_spring.models;

/**
 * Status options for payment transactions
 */
public enum PaymentTransactionStatus {
    PENDING,     // Transaction initiated but not completed
    PROCESSING,  // Payment is being processed
    SUCCESS,     // Payment successful
    FAILED,      // Payment failed
    CANCELLED,   // Transaction cancelled
    REFUNDED     // Payment refunded
}
