package com.reuben.pastcare_spring.models;

/**
 * Status enum for portal user lifecycle
 */
public enum PortalUserStatus {
    PENDING_VERIFICATION,  // Registered but email not verified
    PENDING_APPROVAL,      // Email verified, waiting for admin approval
    APPROVED,              // Approved by admin, active access
    REJECTED,              // Rejected by admin
    SUSPENDED              // Temporarily suspended by admin
}
