package com.reuben.pastcare_spring.models;

/**
 * Enum representing the visibility level of an event.
 * Controls who can view and register for the event.
 */
public enum EventVisibility {
    /**
     * Public event visible to everyone (members, visitors, public)
     */
    PUBLIC("Public", "Visible to everyone including non-members"),

    /**
     * Event visible only to church members
     */
    MEMBERS_ONLY("Members Only", "Visible only to registered members"),

    /**
     * Event visible only to leadership (pastors, admins, leaders)
     */
    LEADERSHIP_ONLY("Leadership Only", "Visible only to church leadership"),

    /**
     * Private event visible only to invited members
     */
    PRIVATE("Private", "Visible only to specifically invited members");

    private final String displayName;
    private final String description;

    EventVisibility(String displayName, String description) {
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
     * Get EventVisibility from string value (case-insensitive)
     */
    public static EventVisibility fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (EventVisibility visibility : EventVisibility.values()) {
            if (visibility.name().equalsIgnoreCase(value.trim())) {
                return visibility;
            }
        }
        throw new IllegalArgumentException("Invalid EventVisibility: " + value);
    }

    /**
     * Check if this visibility level allows public access
     */
    public boolean isPubliclyVisible() {
        return this == PUBLIC;
    }

    /**
     * Check if this visibility level requires authentication
     */
    public boolean requiresAuthentication() {
        return this != PUBLIC;
    }

    /**
     * Check if this visibility level is restricted to leadership
     */
    public boolean isLeadershipOnly() {
        return this == LEADERSHIP_ONLY || this == PRIVATE;
    }
}
