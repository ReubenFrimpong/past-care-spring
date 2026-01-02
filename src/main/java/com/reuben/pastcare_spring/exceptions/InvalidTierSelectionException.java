package com.reuben.pastcare_spring.exceptions;

/**
 * Exception thrown when a church attempts to select a subscription tier
 * that cannot accommodate their current member count.
 *
 * <p>Example: Church with 350 members trying to select TIER_1 (max 200 members)
 *
 * @since 2026-01-02
 */
public class InvalidTierSelectionException extends RuntimeException {

    public InvalidTierSelectionException(String message) {
        super(message);
    }

    public InvalidTierSelectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
