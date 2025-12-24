package com.reuben.pastcare_spring.enums;

/**
 * Status of attendance reminder delivery.
 *
 * Phase 1: Enhanced Attendance Tracking
 */
public enum ReminderStatus {
    /**
     * Reminder is scheduled but not yet sent.
     */
    SCHEDULED,

    /**
     * Reminder has been sent to communication service.
     */
    SENT,

    /**
     * Reminder was successfully delivered to recipients.
     */
    DELIVERED,

    /**
     * Reminder delivery failed (e.g., invalid phone number, email bounced).
     */
    FAILED,

    /**
     * Reminder was cancelled before sending.
     */
    CANCELLED
}
