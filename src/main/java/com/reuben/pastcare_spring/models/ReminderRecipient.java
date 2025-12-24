package com.reuben.pastcare_spring.models;

import java.time.LocalDateTime;

import com.reuben.pastcare_spring.enums.ReminderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ReminderRecipient entity for tracking reminder delivery status per member.
 * Links reminders to individual members with delivery tracking.
 *
 * Phase 1: Enhanced Attendance Tracking - Attendance Reminders
 *
 * Multi-Tenancy: Tenant-scoped through relationships with AttendanceReminder and Member.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(uniqueConstraints = {
  @UniqueConstraint(columnNames = {"reminder_id", "member_id"})
})
public class ReminderRecipient extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "reminder_id", nullable = false)
  private AttendanceReminder reminder;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ReminderStatus status = ReminderStatus.SCHEDULED;

  private LocalDateTime sentAt;

  private LocalDateTime deliveredAt;

  private LocalDateTime failedAt;

  @Column(length = 500)
  private String failureReason;

  private Boolean smsSent = false;

  private Boolean emailSent = false;

  private Boolean whatsappSent = false;
}
