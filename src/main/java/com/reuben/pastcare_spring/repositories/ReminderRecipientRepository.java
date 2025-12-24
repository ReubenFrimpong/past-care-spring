package com.reuben.pastcare_spring.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reuben.pastcare_spring.enums.ReminderStatus;
import com.reuben.pastcare_spring.models.ReminderRecipient;

/**
 * Repository for ReminderRecipient entity.
 * Phase 1: Enhanced Attendance Tracking - Attendance Reminders
 */
@Repository
public interface ReminderRecipientRepository extends JpaRepository<ReminderRecipient, Long> {

  /**
   * Find all recipients for a specific reminder.
   */
  List<ReminderRecipient> findByReminderId(Long reminderId);

  /**
   * Find all reminders for a specific member.
   */
  List<ReminderRecipient> findByMemberId(Long memberId);

  /**
   * Find recipients by status.
   */
  List<ReminderRecipient> findByStatus(ReminderStatus status);

  /**
   * Find recipients by reminder and status.
   */
  List<ReminderRecipient> findByReminderIdAndStatus(Long reminderId, ReminderStatus status);

  /**
   * Count recipients for a reminder.
   */
  long countByReminderId(Long reminderId);

  /**
   * Count recipients by reminder and status.
   */
  long countByReminderIdAndStatus(Long reminderId, ReminderStatus status);
}
