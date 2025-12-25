package com.reuben.pastcare_spring.dtos;

/**
 * Fellowship Module Phase 2: Analytics
 * Response DTO for fellowship analytics
 */
public record FellowshipAnalyticsResponse(
    Long fellowshipId,
    String fellowshipName,
    Integer currentMembers,
    Integer maxCapacity,
    Double occupancyRate, // Percentage (0-100)
    Integer memberGrowthLast30Days, // New members in last 30 days
    Integer memberGrowthLast90Days, // New members in last 90 days
    Double growthRate, // Percentage growth rate
    Integer pendingJoinRequests,
    Boolean isHealthy, // Based on health metrics
    String healthStatus, // EXCELLENT, GOOD, FAIR, AT_RISK
    String growthTrend // GROWING, STABLE, DECLINING
) {
    /**
     * Calculate health status based on metrics
     */
    public static String calculateHealthStatus(Double occupancyRate, Integer memberGrowthLast30Days, Integer currentMembers) {
        // Excellent: Good occupancy, recent growth, decent size
        if (occupancyRate >= 60 && occupancyRate <= 85 && memberGrowthLast30Days > 0 && currentMembers >= 10) {
            return "EXCELLENT";
        }
        // Good: Decent occupancy, stable or growing
        if (occupancyRate >= 40 && occupancyRate <= 90 && memberGrowthLast30Days >= 0 && currentMembers >= 5) {
            return "GOOD";
        }
        // Fair: Low occupancy or declining but not critical
        if (occupancyRate >= 20 || currentMembers >= 3) {
            return "FAIR";
        }
        // At risk: Very low occupancy or very small
        return "AT_RISK";
    }

    /**
     * Determine growth trend based on recent growth
     */
    public static String calculateGrowthTrend(Integer memberGrowthLast30Days, Integer memberGrowthLast90Days) {
        if (memberGrowthLast30Days > 0 || memberGrowthLast90Days > 0) {
            return "GROWING";
        }
        if (memberGrowthLast30Days < 0 || memberGrowthLast90Days < 0) {
            return "DECLINING";
        }
        return "STABLE";
    }
}
