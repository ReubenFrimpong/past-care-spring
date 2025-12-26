package com.reuben.pastcare_spring.models;

/**
 * Status of a care need in the pastoral care workflow
 */
public enum CareNeedStatus {
    PENDING,        // New, not yet assigned
    ASSIGNED,       // Assigned to someone
    IN_PROGRESS,    // Being worked on
    ON_HOLD,        // Temporarily paused
    RESOLVED,       // Successfully resolved
    CLOSED          // Closed (may not be fully resolved)
}
