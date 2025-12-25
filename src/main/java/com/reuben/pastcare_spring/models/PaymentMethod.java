package com.reuben.pastcare_spring.models;

/**
 * Giving Module Phase 1: Donation Recording
 * Enumeration of payment methods
 */
public enum PaymentMethod {
    CASH,            // Cash payment
    CHECK,           // Check/Cheque
    MOBILE_MONEY,    // Mobile money (MTN, Vodafone, AirtelTigo)
    BANK_TRANSFER,   // Bank transfer
    CREDIT_CARD,     // Credit card
    DEBIT_CARD,      // Debit card
    ONLINE,          // Online payment (generic)
    OTHER            // Other payment methods
}
