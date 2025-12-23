package com.reuben.pastcare_spring.models;

/**
 * Types of communication methods used to interact with members.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public enum CommunicationType {
  /**
   * Phone call (voice conversation).
   */
  PHONE_CALL,

  /**
   * Email message.
   */
  EMAIL,

  /**
   * In-person visit (home, hospital, office).
   */
  VISIT,

  /**
   * SMS text message.
   */
  SMS,

  /**
   * WhatsApp message or call.
   */
  WHATSAPP,

  /**
   * Video call (Zoom, Teams, etc.).
   */
  VIDEO_CALL,

  /**
   * Letter or postal mail.
   */
  LETTER,

  /**
   * Social media message (Facebook, Instagram, etc.).
   */
  SOCIAL_MEDIA,

  /**
   * In-app message or notification.
   */
  IN_APP_MESSAGE,

  /**
   * Other communication method.
   */
  OTHER
}
