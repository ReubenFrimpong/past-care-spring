package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Tracks all communication interactions with members.
 * Includes phone calls, emails, visits, SMS, WhatsApp messages, etc.
 * Helps pastors and leaders maintain pastoral care history.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "communication_logs")
public class CommunicationLog extends TenantBaseEntity {

  /**
   * The member this communication is about.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  /**
   * Type of communication.
   * Examples: PHONE_CALL, EMAIL, VISIT, SMS, WHATSAPP, etc.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private CommunicationType communicationType;

  /**
   * Direction of communication.
   * OUTGOING: Church contacted member
   * INCOMING: Member contacted church
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CommunicationDirection direction;

  /**
   * Date and time of the communication.
   */
  @Column(nullable = false)
  private LocalDateTime communicationDate;

  /**
   * Duration of the communication in minutes (for calls and visits).
   */
  private Integer durationMinutes;

  /**
   * Subject or title of the communication.
   * Example: "Follow-up on absence", "Prayer request", "Hospital visit"
   */
  @Column(nullable = false, length = 200)
  private String subject;

  /**
   * Detailed notes about the communication.
   * What was discussed, outcomes, next steps, etc.
   */
  @Column(columnDefinition = "TEXT")
  private String notes;

  /**
   * The user (pastor/leader) who initiated or logged this communication.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  /**
   * Follow-up required after this communication.
   */
  @Column(nullable = false)
  private Boolean followUpRequired = false;

  /**
   * Date when follow-up is due.
   */
  private LocalDateTime followUpDate;

  /**
   * Status of follow-up if required.
   */
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private FollowUpStatus followUpStatus;

  /**
   * Priority level of this communication.
   */
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private CommunicationPriority priority = CommunicationPriority.NORMAL;

  /**
   * Outcome or result of the communication.
   */
  @Column(length = 500)
  private String outcome;

  /**
   * Whether this communication contains confidential information.
   * Only accessible to pastors and authorized leaders.
   */
  @Column(nullable = false)
  private Boolean isConfidential = false;

  /**
   * Tags or categories for this communication.
   * Example: "pastoral-care", "counseling", "follow-up", "prayer-request"
   */
  @Column(length = 500)
  private String tags;

}
