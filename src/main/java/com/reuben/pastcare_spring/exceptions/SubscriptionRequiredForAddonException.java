package com.reuben.pastcare_spring.exceptions;

/**
 * Exception thrown when a church attempts to purchase an addon without an active subscription.
 *
 * <p>This triggers HTTP 402 Payment Required response indicating subscription must be active.
 */
public class SubscriptionRequiredForAddonException extends RuntimeException {

    public SubscriptionRequiredForAddonException(String message) {
        super(message);
    }

    public SubscriptionRequiredForAddonException(Long churchId, String subscriptionStatus) {
        super(String.format(
                "Church %d cannot purchase addons with subscription status: %s. " +
                "Subscription must be ACTIVE or in grace period.",
                churchId, subscriptionStatus
        ));
    }
}
