package com.reuben.pastcare_spring.dtos;

import java.time.Instant;
import java.time.LocalDateTime;

import com.reuben.pastcare_spring.enums.ReminderStatus;

/**
 * Response DTO for reminder recipient details.
 * Phase 1: Enhanced Attendance Tracking - Attendance Reminders
 *
 * @param id Recipient record ID
 * @param reminderId Reminder ID
 * @param memberId Member ID
 * @param memberName Member name
 * @param memberPhone Member phone number
 * @param memberEmail Member email
 * @param status Delivery status
 * @param sentAt When the reminder was sent
 * @param deliveredAt When the reminder was delivered
 * @param failedAt When the delivery failed
 * @param failureReason Reason for failure
 * @param smsSent Whether SMS was sent
 * @param emailSent Whether email was sent
 * @param whatsappSent Whether WhatsApp was sent
 * @param createdAt Record creation timestamp
 * @param updatedAt Last update timestamp
 */
public record RecipientResponse(
    Long id,
    Long reminderId,
    Long memberId,
    String memberName,
    String memberPhone,
    String memberEmail,
    ReminderStatus status,
    LocalDateTime sentAt,
    LocalDateTime deliveredAt,
    LocalDateTime failedAt,
    String failureReason,
    Boolean smsSent,
    Boolean emailSent,
    Boolean whatsappSent,
    Instant createdAt,
    Instant updatedAt
) {
}
