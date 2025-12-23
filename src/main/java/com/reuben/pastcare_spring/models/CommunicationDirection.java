package com.reuben.pastcare_spring.models;

/**
 * Direction of communication flow between church and member.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public enum CommunicationDirection {
  /**
   * Church/pastor initiated contact with member.
   * Example: Pastor called member to check on them.
   */
  OUTGOING,

  /**
   * Member initiated contact with church/pastor.
   * Example: Member called pastor for prayer request.
   */
  INCOMING
}
