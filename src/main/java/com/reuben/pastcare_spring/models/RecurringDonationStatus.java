package com.reuben.pastcare_spring.models;

/**
 * Status options for recurring donations
 */
public enum RecurringDonationStatus {
    ACTIVE,      // Currently processing payments
    PAUSED,      // Temporarily suspended by user
    CANCELLED,   // Cancelled by user or admin
    COMPLETED,   // End date reached
    FAILED       // Too many failed payment attempts
}
