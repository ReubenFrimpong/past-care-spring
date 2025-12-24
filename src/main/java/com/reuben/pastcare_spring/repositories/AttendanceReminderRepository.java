package com.reuben.pastcare_spring.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reuben.pastcare_spring.enums.ReminderStatus;
import com.reuben.pastcare_spring.models.AttendanceReminder;

/**
 * Repository for AttendanceReminder entity.
 * Phase 1: Enhanced Attendance Tracking - Attendance Reminders
 */
@Repository
public interface AttendanceReminderRepository extends JpaRepository<AttendanceReminder, Long> {

  /**
   * Find reminders by status.
   */
  List<AttendanceReminder> findByStatus(ReminderStatus status);

  /**
   * Find reminders scheduled before a certain time.
   */
  List<AttendanceReminder> findByScheduledForBeforeAndStatus(LocalDateTime scheduledFor, ReminderStatus status);

  /**
   * Find reminders by target group.
   */
  List<AttendanceReminder> findByTargetGroup(String targetGroup);

  /**
   * Find reminders by fellowship.
   */
  List<AttendanceReminder> findByFellowshipId(Long fellowshipId);

  /**
   * Find reminders created by a specific user.
   */
  List<AttendanceReminder> findByCreatedByUserId(Long userId);

  /**
   * Find recurring reminders.
   */
  List<AttendanceReminder> findByIsRecurring(Boolean isRecurring);

  /**
   * Find reminders scheduled between two dates.
   */
  List<AttendanceReminder> findByScheduledForBetween(LocalDateTime start, LocalDateTime end);
}
