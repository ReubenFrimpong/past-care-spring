package com.reuben.pastcare_spring.models;

/**
 * Types of care needs that can be tracked in the pastoral care system
 */
public enum CareNeedType {
    HOSPITAL_VISIT,      // Member hospitalized
    BEREAVEMENT,         // Death in family
    COUNSELING,          // General counseling need
    PRAYER,              // Prayer request
    FINANCIAL,           // Financial assistance
    HOUSING,             // Housing issues
    EMPLOYMENT,          // Job loss or employment issues
    FAMILY_CRISIS,       // Family problems
    SPIRITUAL,           // Spiritual struggles
    HEALTH,              // Health concerns (non-hospital)
    EMOTIONAL,           // Mental/emotional support
    MARRIAGE,            // Marriage counseling
    ADDICTION,           // Substance abuse
    LEGAL,               // Legal issues
    OTHER                // Other needs
}
