package com.reuben.pastcare_spring.exceptions;

/**
 * Exception thrown when payment verification with Paystack fails.
 *
 * @since 2026-01-02
 */
public class PaymentVerificationException extends RuntimeException {

    public PaymentVerificationException(String message) {
        super(message);
    }

    public PaymentVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
