package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Platform-wide billing statistics for SUPERADMIN dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformBillingStatsResponse {

    /**
     * Monthly Recurring Revenue (MRR) in GHS
     */
    private Double monthlyRecurringRevenue;

    /**
     * MRR formatted for display
     */
    private String mrrDisplay;

    /**
     * Annual Recurring Revenue (ARR) in GHS
     */
    private Double annualRecurringRevenue;

    /**
     * ARR formatted for display
     */
    private String arrDisplay;

    /**
     * MRR growth percentage (month over month)
     */
    private Double mrrGrowthPercent;

    /**
     * Total active subscriptions
     */
    private Integer activeSubscriptions;

    /**
     * Total past due subscriptions
     */
    private Integer pastDueSubscriptions;

    /**
     * Total canceled subscriptions
     */
    private Integer canceledSubscriptions;

    /**
     * Total suspended subscriptions
     */
    private Integer suspendedSubscriptions;

    /**
     * Subscription distribution by plan
     * Map of plan name -> count
     */
    private Map<String, Integer> subscriptionsByPlan;

    /**
     * Total revenue this month (GHS)
     */
    private Double revenueThisMonth;

    /**
     * Revenue this month formatted
     */
    private String revenueThisMonthDisplay;

    /**
     * Total revenue last month (GHS)
     */
    private Double revenueLastMonth;

    /**
     * Revenue last month formatted
     */
    private String revenueLastMonthDisplay;

    /**
     * Number of churches with overdue payments
     */
    private Integer churchesWithOverduePayments;

    /**
     * Total churches being billed
     */
    private Integer totalBilledChurches;

    /**
     * Average revenue per church (ARPU)
     */
    private Double averageRevenuePerChurch;

    /**
     * ARPU formatted
     */
    private String arpuDisplay;

    /**
     * Churn rate percentage (canceled/total)
     */
    private Double churnRate;
}
