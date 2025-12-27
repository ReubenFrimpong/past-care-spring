package com.reuben.pastcare_spring.models;

/**
 * Outcome of a counseling session
 */
public enum SessionOutcome {
    POSITIVE,           // Positive progress made
    NEUTRAL,            // No significant change
    CHALLENGING,        // Difficult session but productive
    NEEDS_FOLLOWUP,     // Requires immediate follow-up
    NEEDS_REFERRAL,     // Needs professional referral
    RESOLVED,           // Issue resolved
    ONGOING             // Ongoing support needed
}
