package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Stores confidential notes about members.
 * Only accessible to users with specific roles (SUPERADMIN, ADMIN, authorized pastors).
 * Used for sensitive pastoral care information, counseling notes, etc.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "confidential_notes")
public class ConfidentialNote extends TenantBaseEntity {

  /**
   * The member this note is about.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  /**
   * Category of the confidential note.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private ConfidentialNoteCategory category;

  /**
   * Subject or title of the note.
   */
  @Column(nullable = false, length = 200)
  private String subject;

  /**
   * The confidential note content.
   * Encrypted at rest in production.
   */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  /**
   * The user who created this note.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_user_id", nullable = false)
  private User createdBy;

  /**
   * Date and time when the note was last modified.
   */
  private LocalDateTime lastModifiedAt;

  /**
   * The user who last modified this note.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_modified_by_user_id")
  private User lastModifiedBy;

  /**
   * Priority level of this note.
   */
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private CommunicationPriority priority = CommunicationPriority.NORMAL;

  /**
   * Whether this note requires follow-up action.
   */
  @Column(nullable = false)
  private Boolean requiresFollowUp = false;

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
   * Minimum role level required to view this note.
   * Example: ADMIN (only admins and superadmins can view)
   */
  @Column(length = 50)
  private String minimumRoleRequired;

  /**
   * Whether this note has been archived.
   * Archived notes are hidden by default but can be retrieved.
   */
  @Column(nullable = false)
  private Boolean isArchived = false;

  /**
   * Related communication log entry (if applicable).
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "communication_log_id")
  private CommunicationLog relatedCommunication;

  /**
   * Tags for categorizing and searching notes.
   */
  @Column(length = 500)
  private String tags;

}
