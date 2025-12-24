package com.reuben.pastcare_spring.dtos;

import java.time.Instant;
import java.time.LocalDateTime;

import com.reuben.pastcare_spring.enums.ReminderStatus;

/**
 * Response DTO for attendance reminder details.
 * Phase 1: Enhanced Attendance Tracking - Attendance Reminders
 *
 * @param id Reminder ID
 * @param createdByUserId User who created the reminder
 * @param createdByUserName Name of user who created the reminder
 * @param message The reminder message
 * @param targetGroup Target group description
 * @param fellowshipId Fellowship ID if targeting specific fellowship
 * @param fellowshipName Fellowship name if targeting specific fellowship
 * @param scheduledFor When the reminder is scheduled to send
 * @param status Current status of the reminder
 * @param sendViaSms Whether to send via SMS
 * @param sendViaEmail Whether to send via email
 * @param sendViaWhatsapp Whether to send via WhatsApp
 * @param isRecurring Whether this is a recurring reminder
 * @param recurrencePattern Recurrence pattern
 * @param recipientCount Total number of recipients
 * @param sentCount Number of reminders sent
 * @param deliveredCount Number of reminders delivered
 * @param failedCount Number of reminders failed
 * @param sentAt When the reminder was sent
 * @param cancelledAt When the reminder was cancelled
 * @param cancelledByUserId User who cancelled the reminder
 * @param cancelledByUserName Name of user who cancelled
 * @param notes Additional notes
 * @param createdAt Record creation timestamp
 * @param updatedAt Last update timestamp
 */
public record ReminderResponse(
    Long id,
    Long createdByUserId,
    String createdByUserName,
    String message,
    String targetGroup,
    Long fellowshipId,
    String fellowshipName,
    LocalDateTime scheduledFor,
    ReminderStatus status,
    Boolean sendViaSms,
    Boolean sendViaEmail,
    Boolean sendViaWhatsapp,
    Boolean isRecurring,
    String recurrencePattern,
    Integer recipientCount,
    Integer sentCount,
    Integer deliveredCount,
    Integer failedCount,
    LocalDateTime sentAt,
    LocalDateTime cancelledAt,
    Long cancelledByUserId,
    String cancelledByUserName,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {
}
