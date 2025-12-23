package com.reuben.pastcare_spring.models;

/**
 * Types of lifecycle events that can occur in a member's spiritual journey.
 * Used to categorize significant milestones and ceremonies.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public enum LifecycleEventType {
  /**
   * Water baptism ceremony.
   * Signifies public declaration of faith and commitment.
   */
  BAPTISM,

  /**
   * Confirmation ceremony.
   * Confirms faith and commitment, often after baptism.
   */
  CONFIRMATION,

  /**
   * Official membership into the church.
   * Completed membership class and committed to church covenant.
   */
  MEMBERSHIP,

  /**
   * Child dedication ceremony.
   * Parents dedicate child to God, committing to raise them in faith.
   */
  CHILD_DEDICATION,

  /**
   * Baby dedication ceremony.
   * Similar to child dedication but for infants.
   */
  BABY_DEDICATION,

  /**
   * Ordination ceremony.
   * Set apart for ministry (pastor, elder, deacon, etc.).
   */
  ORDINATION,

  /**
   * Commissioning for ministry or mission.
   * Sent out for specific ministry purpose.
   */
  COMMISSIONING,

  /**
   * Wedding/marriage ceremony.
   * Holy matrimony performed at the church.
   */
  WEDDING,

  /**
   * Funeral service.
   * Memorial service for deceased member.
   */
  FUNERAL,

  /**
   * First communion.
   * First time partaking in the Lord's Supper.
   */
  FIRST_COMMUNION,

  /**
   * Graduation from ministry training or Bible school.
   */
  MINISTRY_TRAINING_GRADUATION,

  /**
   * Conversion/Salvation testimony.
   * Record of when member accepted Christ.
   */
  SALVATION,

  /**
   * Baptism in the Holy Spirit.
   * Separate event from water baptism in some traditions.
   */
  HOLY_SPIRIT_BAPTISM,

  /**
   * Transfer from another church.
   * Officially transferred membership to this church.
   */
  TRANSFER_IN,

  /**
   * Transfer to another church.
   * Officially transferred membership to another church.
   */
  TRANSFER_OUT,

  /**
   * Restoration after discipline.
   * Member restored to full fellowship after disciplinary period.
   */
  RESTORATION,

  /**
   * Other lifecycle event not covered by standard types.
   */
  OTHER
}
