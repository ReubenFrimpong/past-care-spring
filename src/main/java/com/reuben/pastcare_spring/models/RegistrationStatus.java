package com.reuben.pastcare_spring.models;

/**
 * Enum representing the status of an event registration.
 * Supports approval workflow and waitlist management.
 */
public enum RegistrationStatus {
    /**
     * Registration submitted, pending approval
     */
    PENDING("Pending", "Registration submitted, awaiting approval"),

    /**
     * Registration approved and confirmed
     */
    APPROVED("Approved", "Registration approved and confirmed"),

    /**
     * Registration rejected
     */
    REJECTED("Rejected", "Registration was rejected"),

    /**
     * Registration cancelled by the registrant or admin
     */
    CANCELLED("Cancelled", "Registration was cancelled"),

    /**
     * Member attended the event
     */
    ATTENDED("Attended", "Member attended the event"),

    /**
     * Member registered but did not attend
     */
    NO_SHOW("No Show", "Member did not attend despite registration");

    private final String displayName;
    private final String description;

    RegistrationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get RegistrationStatus from string value (case-insensitive)
     */
    public static RegistrationStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (RegistrationStatus status : RegistrationStatus.values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid RegistrationStatus: " + value);
    }

    /**
     * Check if this status represents an active registration
     */
    public boolean isActive() {
        return this == PENDING || this == APPROVED;
    }

    /**
     * Check if this status represents a completed event
     */
    public boolean isCompleted() {
        return this == ATTENDED || this == NO_SHOW;
    }

    /**
     * Check if this status allows modification
     */
    public boolean canBeModified() {
        return this == PENDING || this == APPROVED;
    }

    /**
     * Check if this status counts towards event capacity
     */
    public boolean countsTowardsCapacity() {
        return this == PENDING || this == APPROVED || this == ATTENDED;
    }
}
