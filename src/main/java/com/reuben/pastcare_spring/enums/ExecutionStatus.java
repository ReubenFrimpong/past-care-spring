package com.reuben.pastcare_spring.enums;

/**
 * Enumeration of report execution statuses.
 */
public enum ExecutionStatus {
    PENDING("Pending", "Report generation queued"),
    RUNNING("Running", "Report is being generated"),
    COMPLETED("Completed", "Report generated successfully"),
    FAILED("Failed", "Report generation failed");

    private final String displayName;
    private final String description;

    ExecutionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
