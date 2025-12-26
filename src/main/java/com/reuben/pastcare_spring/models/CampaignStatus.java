package com.reuben.pastcare_spring.models;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * Campaign status enumeration
 */
public enum CampaignStatus {
  /**
   * Campaign is currently active and accepting donations
   */
  ACTIVE,

  /**
   * Campaign is temporarily paused
   */
  PAUSED,

  /**
   * Campaign has successfully completed (goal reached or end date passed)
   */
  COMPLETED,

  /**
   * Campaign has been cancelled
   */
  CANCELLED
}
