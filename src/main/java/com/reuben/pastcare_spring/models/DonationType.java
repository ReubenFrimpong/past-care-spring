package com.reuben.pastcare_spring.models;

/**
 * Giving Module Phase 1: Donation Recording
 * Enumeration of donation types
 */
public enum DonationType {
    TITHE,           // Regular tithe (10% of income)
    OFFERING,        // General offering
    SPECIAL_GIVING,  // Special offerings (missions, projects, etc.)
    PLEDGE,          // Pledge payment
    MISSIONS,        // Missions donation
    BUILDING_FUND,   // Building/construction fund
    BENEVOLENCE,     // Helping the needy
    OTHER            // Other donations
}
