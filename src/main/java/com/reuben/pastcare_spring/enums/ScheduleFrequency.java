package com.reuben.pastcare_spring.enums;

/**
 * Enumeration of report scheduling frequencies.
 */
public enum ScheduleFrequency {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    YEARLY("Yearly", 365);

    private final String displayName;
    private final int approximateDays;

    ScheduleFrequency(String displayName, int approximateDays) {
        this.displayName = displayName;
        this.approximateDays = approximateDays;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getApproximateDays() {
        return approximateDays;
    }
}
