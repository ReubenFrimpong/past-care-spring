package com.reuben.pastcare_spring.models;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * Pledge status enumeration
 */
public enum PledgeStatus {
  /**
   * Pledge is active and member is making payments
   */
  ACTIVE,

  /**
   * Pledge has been fully paid
   */
  COMPLETED,

  /**
   * Pledge has been cancelled by member or admin
   */
  CANCELLED,

  /**
   * Pledge payment has been missed or is significantly overdue
   */
  DEFAULTED
}
