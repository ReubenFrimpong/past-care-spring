package com.reuben.pastcare_spring.models;

public enum CareNeedStatus {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    PENDING("Pending"),
    RESOLVED("Resolved"),
    CLOSED("Closed"),
    CANCELLED("Cancelled");

    private final String displayName;

    CareNeedStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
