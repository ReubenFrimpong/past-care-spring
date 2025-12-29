package com.reuben.pastcare_spring.enums;

/**
 * Status of a goal.
 * Dashboard Phase 2.3: Goal Tracking
 */
public enum GoalStatus {
    ACTIVE,      // Goal is currently in progress
    COMPLETED,   // Goal was successfully achieved
    FAILED,      // Goal period ended without achieving target
    CANCELLED    // Goal was manually cancelled
}
