package com.reuben.pastcare_spring.models;

/**
 * Status for prayer requests
 */
public enum PrayerRequestStatus {
    PENDING,    // Newly submitted, awaiting review
    ACTIVE,     // Currently being prayed for
    ANSWERED,   // Prayer answered
    ARCHIVED    // Archived/expired
}
