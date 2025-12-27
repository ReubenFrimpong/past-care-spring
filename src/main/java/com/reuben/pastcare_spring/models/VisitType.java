package com.reuben.pastcare_spring.models;

public enum VisitType {
    HOME("Home Visit"),
    HOSPITAL("Hospital Visit"),
    OFFICE("Office Visit"),
    PHONE("Phone Call"),
    VIDEO("Video Call"),
    OTHER("Other");

    private final String displayName;

    VisitType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
