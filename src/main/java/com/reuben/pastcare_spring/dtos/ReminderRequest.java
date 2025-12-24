package com.reuben.pastcare_spring.dtos;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating/updating an attendance reminder.
 * Phase 1: Enhanced Attendance Tracking - Attendance Reminders
 *
 * @param message The reminder message to send
 * @param targetGroup Target group (e.g., "IRREGULAR_ATTENDERS", "ALL_MEMBERS", "FELLOWSHIP")
 * @param fellowshipId Fellowship ID if targeting specific fellowship
 * @param specificMemberIds List of specific member IDs to target (optional)
 * @param scheduledFor When to send the reminder
 * @param sendViaSms Send via SMS
 * @param sendViaEmail Send via email
 * @param sendViaWhatsapp Send via WhatsApp
 * @param isRecurring Whether this is a recurring reminder
 * @param recurrencePattern Recurrence pattern (e.g., "DAILY", "WEEKLY", "MONTHLY")
 * @param notes Additional notes about the reminder
 */
public record ReminderRequest(
    @NotBlank(message = "Message is required")
    String message,

    @NotBlank(message = "Target group is required")
    @Size(max = 50, message = "Target group must not exceed 50 characters")
    String targetGroup,

    Long fellowshipId,

    List<Long> specificMemberIds,

    @NotNull(message = "Scheduled time is required")
    LocalDateTime scheduledFor,

    Boolean sendViaSms,

    Boolean sendViaEmail,

    Boolean sendViaWhatsapp,

    Boolean isRecurring,

    @Size(max = 100, message = "Recurrence pattern must not exceed 100 characters")
    String recurrencePattern,

    String notes
) {
  public ReminderRequest {
    // Default values
    if (sendViaSms == null) sendViaSms = false;
    if (sendViaEmail == null) sendViaEmail = false;
    if (sendViaWhatsapp == null) sendViaWhatsapp = false;
    if (isRecurring == null) isRecurring = false;
  }

  /**
   * Validate that at least one communication channel is selected.
   */
  public void validate() {
    if (!sendViaSms && !sendViaEmail && !sendViaWhatsapp) {
      throw new IllegalArgumentException("At least one communication channel (SMS, Email, or WhatsApp) must be selected");
    }

    if (isRecurring && (recurrencePattern == null || recurrencePattern.isBlank())) {
      throw new IllegalArgumentException("Recurrence pattern is required for recurring reminders");
    }
  }
}
