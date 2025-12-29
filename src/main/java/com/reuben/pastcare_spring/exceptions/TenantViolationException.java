package com.reuben.pastcare_spring.exceptions;

/**
 * Exception thrown when a user attempts to access data from a different church (tenant).
 * This is a critical security violation that should be logged and monitored.
 *
 * Examples:
 * - User from church A tries to access member from church B
 * - JWT churchId doesn't match resource's churchId
 * - Attempting to modify data outside tenant scope
 */
public class TenantViolationException extends RuntimeException {

    private final Long attemptedChurchId;
    private final Long actualChurchId;
    private final Long userId;
    private final String resourceType;

    public TenantViolationException(String message) {
        super(message);
        this.attemptedChurchId = null;
        this.actualChurchId = null;
        this.userId = null;
        this.resourceType = null;
    }

    public TenantViolationException(String message, Long userId, Long attemptedChurchId, Long actualChurchId, String resourceType) {
        super(String.format("%s - User %d (church %d) attempted to access %s from church %d",
                message, userId, actualChurchId, resourceType, attemptedChurchId));
        this.userId = userId;
        this.attemptedChurchId = attemptedChurchId;
        this.actualChurchId = actualChurchId;
        this.resourceType = resourceType;
    }

    public Long getAttemptedChurchId() {
        return attemptedChurchId;
    }

    public Long getActualChurchId() {
        return actualChurchId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getResourceType() {
        return resourceType;
    }
}
