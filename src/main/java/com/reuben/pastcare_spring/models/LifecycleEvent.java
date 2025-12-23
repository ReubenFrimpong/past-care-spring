package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Represents significant lifecycle events in a member's spiritual journey.
 * Examples: baptism, confirmation, membership, dedication, ordination, etc.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "lifecycle_events")
public class LifecycleEvent extends TenantBaseEntity {

  /**
   * The member this lifecycle event belongs to.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  /**
   * Type of lifecycle event.
   * Examples: BAPTISM, CONFIRMATION, MEMBERSHIP, DEDICATION, ORDINATION, etc.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private LifecycleEventType eventType;

  /**
   * Date when the event occurred.
   */
  @Column(nullable = false)
  private LocalDate eventDate;

  /**
   * Location where the event took place.
   * Example: "Main Church Sanctuary", "River Jordan", "Community Hall"
   */
  @Column(length = 200)
  private String location;

  /**
   * Name of the officiating minister or pastor.
   */
  @Column(length = 100)
  private String officiatingMinister;

  /**
   * Certificate number if applicable.
   * Example: Certificate of Baptism #BT-2024-001
   */
  @Column(length = 100)
  private String certificateNumber;

  /**
   * Additional notes or details about the event.
   */
  @Column(columnDefinition = "TEXT")
  private String notes;

  /**
   * URL to certificate document or photo of the event.
   */
  @Column(length = 500)
  private String documentUrl;

  /**
   * Witnesses present at the event (comma-separated names).
   * Example: "John Doe, Jane Smith"
   */
  @Column(length = 500)
  private String witnesses;

  /**
   * Whether this event is verified/confirmed by church leadership.
   */
  @Column(nullable = false)
  private Boolean isVerified = false;

  /**
   * The user who verified this event.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "verified_by_user_id")
  private User verifiedBy;

}
