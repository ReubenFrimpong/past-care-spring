package com.reuben.pastcare_spring.models;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * Pledge payment status enumeration
 */
public enum PledgePaymentStatus {
  /**
   * Payment is pending/expected
   */
  PENDING,

  /**
   * Payment has been received
   */
  PAID,

  /**
   * Payment is overdue
   */
  LATE,

  /**
   * Payment was missed/skipped
   */
  MISSED,

  /**
   * Payment was cancelled
   */
  CANCELLED
}
