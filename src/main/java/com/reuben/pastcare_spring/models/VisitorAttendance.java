package com.reuben.pastcare_spring.models;

import java.time.LocalDateTime;

import com.reuben.pastcare_spring.enums.CheckInMethod;

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
 * VisitorAttendance entity for tracking visitor attendance at sessions.
 * Links visitors to attendance sessions with check-in details.
 *
 * Phase 1: Enhanced Attendance Tracking - Visitor Management
 *
 * Multi-Tenancy: Tenant-scoped through relationships with Visitor and AttendanceSession.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(uniqueConstraints = {
  @UniqueConstraint(columnNames = {"visitor_id", "attendance_session_id"})
})
public class VisitorAttendance extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "visitor_id", nullable = false)
  private Visitor visitor;

  @ManyToOne
  @JoinColumn(name = "attendance_session_id", nullable = false)
  private AttendanceSession attendanceSession;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private CheckInMethod checkInMethod = CheckInMethod.MANUAL;

  private LocalDateTime checkInTime;

  private Boolean isLate = false;

  private Integer minutesLate;

  @Column(precision = 10, scale = 8)
  private Double checkInLocationLat;

  @Column(precision = 11, scale = 8)
  private Double checkInLocationLong;

  @Column(length = 200)
  private String deviceInfo;

  @Column(columnDefinition = "TEXT")
  private String remarks;
}
