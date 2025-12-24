package com.reuben.pastcare_spring.models;

import java.time.LocalDate;

import com.reuben.pastcare_spring.enums.AgeGroup;
import com.reuben.pastcare_spring.enums.VisitorSource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Visitor entity for tracking guests/visitors before they become members.
 *
 * Phase 1: Enhanced Attendance Tracking - Visitor Management
 *
 * Multi-Tenancy: Extends TenantBaseEntity for automatic church-based filtering.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Visitor extends TenantBaseEntity {

  @Column(nullable = false, length = 100)
  private String firstName;

  @Column(nullable = false, length = 100)
  private String lastName;

  @Column(unique = true, nullable = false, length = 20)
  private String phoneNumber;

  @Column(length = 100)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private AgeGroup ageGroup;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private VisitorSource howHeardAboutUs;

  @ManyToOne
  @JoinColumn(name = "invited_by_member_id")
  private Member invitedByMember;

  private Boolean isFirstTime = true;

  private Integer visitCount = 0;

  private LocalDate lastVisitDate;

  @ManyToOne
  @JoinColumn(name = "assigned_to_user_id")
  private User assignedToUser;

  @Column(length = 30)
  private String followUpStatus;

  private Boolean convertedToMember = false;

  @ManyToOne
  @JoinColumn(name = "converted_member_id")
  private Member convertedMember;

  private LocalDate conversionDate;

  @Column(columnDefinition = "TEXT")
  private String notes;
}
