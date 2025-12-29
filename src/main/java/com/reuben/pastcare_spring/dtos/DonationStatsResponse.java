package com.reuben.pastcare_spring.dtos;

import java.math.BigDecimal;

/**
 * Donation statistics for dashboard.
 */
public record DonationStatsResponse(
    int totalDonations,
    BigDecimal totalAmount,
    int thisWeekCount,
    BigDecimal thisWeekAmount,
    int thisMonthCount,
    BigDecimal thisMonthAmount
) {
}
