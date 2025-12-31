package com.reuben.pastcare_spring.exceptions;

/**
 * Exception thrown when a church attempts to purchase an addon they already have.
 *
 * <p>This triggers HTTP 409 Conflict response indicating the addon is already active.
 */
public class AddonAlreadyPurchasedException extends RuntimeException {

    public AddonAlreadyPurchasedException(String message) {
        super(message);
    }

    public AddonAlreadyPurchasedException(String addonName, Long churchId) {
        super(String.format(
                "Church %d has already purchased addon: %s. " +
                "Please cancel the existing addon before purchasing again.",
                churchId, addonName
        ));
    }
}
