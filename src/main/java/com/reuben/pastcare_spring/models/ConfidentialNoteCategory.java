package com.reuben.pastcare_spring.models;

/**
 * Categories of confidential notes for organizing sensitive member information.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public enum ConfidentialNoteCategory {
  /**
   * Personal counseling session notes.
   */
  COUNSELING,

  /**
   * Marital or family counseling.
   */
  MARRIAGE_COUNSELING,

  /**
   * Financial assistance or counseling.
   */
  FINANCIAL,

  /**
   * Health or medical related information.
   */
  HEALTH,

  /**
   * Legal matters or issues.
   */
  LEGAL,

  /**
   * Disciplinary action or correction.
   */
  DISCIPLINE,

  /**
   * Pastoral care concern or intervention.
   */
  PASTORAL_CARE,

  /**
   * Crisis situation (domestic abuse, suicide risk, etc.).
   */
  CRISIS,

  /**
   * Spiritual struggle or faith crisis.
   */
  SPIRITUAL,

  /**
   * Addiction or recovery related.
   */
  ADDICTION,

  /**
   * Grief or bereavement counseling.
   */
  GRIEF,

  /**
   * Mental health related information.
   */
  MENTAL_HEALTH,

  /**
   * Sensitive family or relationship issue.
   */
  FAMILY_ISSUE,

  /**
   * Confidential prayer request.
   */
  PRAYER_REQUEST,

  /**
   * Other confidential information.
   */
  OTHER
}
