package com.reuben.pastcare_spring.models;

/**
 * Priority level of communication or follow-up action.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public enum CommunicationPriority {
  /**
   * Low priority - routine communication.
   */
  LOW,

  /**
   * Normal priority - standard follow-up.
   */
  NORMAL,

  /**
   * High priority - important matter requiring timely attention.
   */
  HIGH,

  /**
   * Urgent priority - critical situation requiring immediate action.
   */
  URGENT
}
