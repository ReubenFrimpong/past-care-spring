package com.reuben.pastcare_spring.dtos;

/**
 * Response DTO for member growth trend.
 * Dashboard Phase 1: Enhanced Widgets
 */
public record MemberGrowthResponse(
    String month, // e.g., "Jan 2025"
    Long newMembers,
    Long totalMembers
) {
}
