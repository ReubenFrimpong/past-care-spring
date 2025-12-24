package com.reuben.pastcare_spring.enums;

/**
 * How visitors heard about the church.
 * Used for tracking marketing effectiveness and visitor sources.
 *
 * Phase 1: Enhanced Attendance Tracking
 */
public enum VisitorSource {
    /**
     * Invited by friend or family member.
     */
    FRIEND_FAMILY,

    /**
     * Found church through social media (Facebook, Instagram, etc.).
     */
    SOCIAL_MEDIA,

    /**
     * Found church website via search or direct visit.
     */
    WEBSITE,

    /**
     * Saw flyer, poster, or banner.
     */
    FLYER_POSTER,

    /**
     * Heard about church on radio or TV.
     */
    RADIO_TV,

    /**
     * Found church through Google search or maps.
     */
    GOOGLE_SEARCH,

    /**
     * Walk-in visitor (passing by).
     */
    WALK_IN,

    /**
     * Personally invited by a church member.
     */
    INVITED_BY_MEMBER,

    /**
     * Other sources not listed above.
     */
    OTHER
}
