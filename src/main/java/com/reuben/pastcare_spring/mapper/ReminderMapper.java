package com.reuben.pastcare_spring.mapper;

import com.reuben.pastcare_spring.dtos.RecipientResponse;
import com.reuben.pastcare_spring.dtos.ReminderResponse;
import com.reuben.pastcare_spring.models.AttendanceReminder;
import com.reuben.pastcare_spring.models.ReminderRecipient;

/**
 * Mapper for converting between Reminder entities and DTOs.
 * Phase 1: Enhanced Attendance Tracking - Attendance Reminders
 *
 * @author Claude Sonnet 4.5
 * @version 1.0
 * @since 2025-12-24
 */
public class ReminderMapper {

  /**
   * Convert AttendanceReminder entity to ReminderResponse DTO.
   *
   * @param reminder The reminder entity
   * @return ReminderResponse DTO
   */
  public static ReminderResponse toReminderResponse(AttendanceReminder reminder) {
    if (reminder == null) {
      return null;
    }

    return new ReminderResponse(
        reminder.getId(),
        reminder.getCreatedByUser() != null ? reminder.getCreatedByUser().getId() : null,
        reminder.getCreatedByUser() != null ? reminder.getCreatedByUser().getName() : null,
        reminder.getMessage(),
        reminder.getTargetGroup(),
        reminder.getFellowship() != null ? reminder.getFellowship().getId() : null,
        reminder.getFellowship() != null ? reminder.getFellowship().getName() : null,
        reminder.getScheduledFor(),
        reminder.getStatus(),
        reminder.getSendViaSms(),
        reminder.getSendViaEmail(),
        reminder.getSendViaWhatsapp(),
        reminder.getIsRecurring(),
        reminder.getRecurrencePattern(),
        reminder.getRecipientCount(),
        reminder.getSentCount(),
        reminder.getDeliveredCount(),
        reminder.getFailedCount(),
        reminder.getSentAt(),
        reminder.getCancelledAt(),
        reminder.getCancelledByUser() != null ? reminder.getCancelledByUser().getId() : null,
        reminder.getCancelledByUser() != null ? reminder.getCancelledByUser().getName() : null,
        reminder.getNotes(),
        reminder.getCreatedAt(),
        reminder.getUpdatedAt()
    );
  }

  /**
   * Convert ReminderRecipient entity to RecipientResponse DTO.
   *
   * @param recipient The recipient entity
   * @return RecipientResponse DTO
   */
  public static RecipientResponse toRecipientResponse(ReminderRecipient recipient) {
    if (recipient == null) {
      return null;
    }

    return new RecipientResponse(
        recipient.getId(),
        recipient.getReminder() != null ? recipient.getReminder().getId() : null,
        recipient.getMember() != null ? recipient.getMember().getId() : null,
        recipient.getMember() != null
            ? recipient.getMember().getFirstName() + " " + recipient.getMember().getLastName()
            : null,
        recipient.getMember() != null ? recipient.getMember().getPhoneNumber() : null,
        null, // Member entity doesn't have email field
        recipient.getStatus(),
        recipient.getSentAt(),
        recipient.getDeliveredAt(),
        recipient.getFailedAt(),
        recipient.getFailureReason(),
        recipient.getSmsSent(),
        recipient.getEmailSent(),
        recipient.getWhatsappSent(),
        recipient.getCreatedAt(),
        recipient.getUpdatedAt()
    );
  }
}
