package com.reuben.pastcare_spring.models;

/**
 * Enum representing different types of church events.
 * Used to categorize events for filtering, reporting, and display purposes.
 */
public enum EventType {
    /**
     * Regular church services (Sunday service, midweek service, etc.)
     */
    SERVICE("Service", "Regular worship services"),

    /**
     * Conferences, seminars, and training events
     */
    CONFERENCE("Conference", "Conferences and seminars"),

    /**
     * Outreach and evangelism activities
     */
    OUTREACH("Outreach", "Evangelism and community outreach"),

    /**
     * Social gatherings and fellowship events
     */
    SOCIAL("Social", "Social gatherings and fellowship"),

    /**
     * Training sessions and workshops
     */
    TRAINING("Training", "Training sessions and workshops"),

    /**
     * Meetings (leadership, committee, small group, etc.)
     */
    MEETING("Meeting", "Meetings and planning sessions"),

    /**
     * Prayer meetings and prayer vigils
     */
    PRAYER("Prayer", "Prayer meetings and vigils"),

    /**
     * Fundraising events
     */
    FUNDRAISER("Fundraiser", "Fundraising activities"),

    /**
     * Youth-specific events
     */
    YOUTH("Youth", "Youth ministry events"),

    /**
     * Children's ministry events
     */
    CHILDREN("Children", "Children's ministry events"),

    /**
     * Women's ministry events
     */
    WOMEN("Women", "Women's ministry events"),

    /**
     * Men's ministry events
     */
    MEN("Men", "Men's ministry events"),

    /**
     * Special celebrations (anniversaries, holidays, etc.)
     */
    CELEBRATION("Celebration", "Special celebrations"),

    /**
     * Other event types not covered above
     */
    OTHER("Other", "Other event types");

    private final String displayName;
    private final String description;

    EventType(String displayName, String description) {
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
     * Get EventType from string value (case-insensitive)
     */
    public static EventType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (EventType type : EventType.values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid EventType: " + value);
    }
}
