package com.reuben.pastcare_spring.models;

/**
 * Status of a counseling session
 */
public enum CounselingStatus {
    SCHEDULED,   // Session is scheduled
    IN_PROGRESS, // Session is currently happening
    COMPLETED,   // Session completed
    CANCELLED,   // Session was cancelled
    NO_SHOW,     // Member didn't show up
    RESCHEDULED  // Session was rescheduled
}
