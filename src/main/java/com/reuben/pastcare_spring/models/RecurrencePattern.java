package com.reuben.pastcare_spring.models;

/**
 * Enum representing recurrence patterns for repeating events.
 * Supports various frequency patterns for automated event creation.
 */
public enum RecurrencePattern {
    /**
     * Event recurs daily
     */
    DAILY("Daily", "Event repeats every day"),

    /**
     * Event recurs weekly on the same day of the week
     */
    WEEKLY("Weekly", "Event repeats every week"),

    /**
     * Event recurs bi-weekly (every 2 weeks)
     */
    BI_WEEKLY("Bi-weekly", "Event repeats every 2 weeks"),

    /**
     * Event recurs monthly on the same day
     */
    MONTHLY("Monthly", "Event repeats monthly"),

    /**
     * Event recurs quarterly (every 3 months)
     */
    QUARTERLY("Quarterly", "Event repeats every 3 months"),

    /**
     * Event recurs annually on the same date
     */
    YEARLY("Yearly", "Event repeats yearly"),

    /**
     * Custom recurrence pattern
     */
    CUSTOM("Custom", "Custom recurrence pattern");

    private final String displayName;
    private final String description;

    RecurrencePattern(String displayName, String description) {
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
     * Get RecurrencePattern from string value (case-insensitive)
     */
    public static RecurrencePattern fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (RecurrencePattern pattern : RecurrencePattern.values()) {
            if (pattern.name().equalsIgnoreCase(value.trim())) {
                return pattern;
            }
        }
        throw new IllegalArgumentException("Invalid RecurrencePattern: " + value);
    }

    /**
     * Get the number of days to add for the next occurrence
     * Returns null for CUSTOM pattern
     */
    public Integer getDaysToAdd() {
        return switch (this) {
            case DAILY -> 1;
            case WEEKLY -> 7;
            case BI_WEEKLY -> 14;
            case MONTHLY -> 30; // Approximate
            case QUARTERLY -> 90; // Approximate
            case YEARLY -> 365; // Approximate
            case CUSTOM -> null;
        };
    }

    /**
     * Check if this pattern requires exact date calculation
     * (as opposed to simple day addition)
     */
    public boolean requiresExactDateCalculation() {
        return this == MONTHLY || this == QUARTERLY || this == YEARLY;
    }
}
