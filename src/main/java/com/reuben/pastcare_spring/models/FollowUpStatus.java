package com.reuben.pastcare_spring.models;

/**
 * Status of follow-up actions required after communication.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public enum FollowUpStatus {
  /**
   * Follow-up is pending and not yet started.
   */
  PENDING,

  /**
   * Follow-up is in progress.
   */
  IN_PROGRESS,

  /**
   * Follow-up has been completed successfully.
   */
  COMPLETED,

  /**
   * Follow-up is overdue (past due date).
   */
  OVERDUE,

  /**
   * Follow-up has been cancelled or is no longer needed.
   */
  CANCELLED
}
