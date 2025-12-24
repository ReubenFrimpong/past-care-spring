package com.reuben.pastcare_spring.models;

import java.time.LocalDateTime;

import com.reuben.pastcare_spring.enums.ReminderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AttendanceReminder entity for scheduling and tracking attendance reminders.
 * Used to send reminders to irregular attenders or specific groups.
 *
 * Phase 1: Enhanced Attendance Tracking - Attendance Reminders
 *
 * Multi-Tenancy: Extends TenantBaseEntity for automatic church-based filtering.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class AttendanceReminder extends TenantBaseEntity {

  @ManyToOne
  @JoinColumn(name = "created_by_user_id", nullable = false)
  private User createdByUser;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String message;

  @Column(nullable = false, length = 50)
  private String targetGroup;

  @ManyToOne
  @JoinColumn(name = "fellowship_id")
  private Fellowship fellowship;

  @Column(nullable = false)
  private LocalDateTime scheduledFor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ReminderStatus status = ReminderStatus.SCHEDULED;

  private Boolean sendViaSms = false;

  private Boolean sendViaEmail = false;

  private Boolean sendViaWhatsapp = false;

  private Boolean isRecurring = false;

  @Column(length = 100)
  private String recurrencePattern;

  private Integer recipientCount = 0;

  private Integer sentCount = 0;

  private Integer deliveredCount = 0;

  private Integer failedCount = 0;

  private LocalDateTime sentAt;

  private LocalDateTime cancelledAt;

  @ManyToOne
  @JoinColumn(name = "cancelled_by_user_id")
  private User cancelledByUser;

  @Column(columnDefinition = "TEXT")
  private String notes;
}
