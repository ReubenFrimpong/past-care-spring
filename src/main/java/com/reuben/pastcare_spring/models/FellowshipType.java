package com.reuben.pastcare_spring.models;

/**
 * Enum representing different types of fellowships/small groups in the church.
 */
public enum FellowshipType {
  /**
   * Age-based fellowship (e.g., Youth, Young Adults, Seniors)
   */
  AGE_BASED,

  /**
   * Interest-based fellowship (e.g., Sports, Music, Bible Study)
   */
  INTEREST_BASED,

  /**
   * Geographic-based fellowship (e.g., by neighborhood, city area)
   */
  GEOGRAPHIC,

  /**
   * Ministry-based fellowship (e.g., Worship Team, Ushering Team)
   */
  MINISTRY,

  /**
   * Gender-based fellowship (e.g., Men's Fellowship, Women's Fellowship)
   */
  GENDER_BASED,

  /**
   * Family/Marital status-based (e.g., Married Couples, Singles)
   */
  FAMILY_BASED,

  /**
   * Other/General fellowship type
   */
  OTHER
}
