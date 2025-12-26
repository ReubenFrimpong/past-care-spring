package com.reuben.pastcare_spring.dtos;

/**
 * Response DTO for care need statistics
 */
public record CareNeedStatsResponse(
    long totalCareNeeds,
    long pendingCount,
    long inProgressCount,
    long resolvedCount,
    long urgentCount,
    long overdueCount
) {}
