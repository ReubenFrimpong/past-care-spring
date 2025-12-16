package com.reuben.pastcare_spring.models;

import com.reuben.pastcare_spring.enums.AttendanceStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Attendance record for a member in a specific session.
 *
 * Multi-Tenancy: This entity is tenant-scoped through its relationships:
 * - Member (extends TenantBaseEntity with church_id)
 * - AttendanceSession (extends TenantBaseEntity with church_id)
 *
 * No explicit church_id needed as it's inherited from the relationships.
 * Queries are automatically scoped by the member's or session's church.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Attendance extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne
  @JoinColumn(name = "attendance_session_id", nullable = false)
  private AttendanceSession attendanceSession;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AttendanceStatus status;

  @Column(columnDefinition = "TEXT")
  private String remarks;
}
