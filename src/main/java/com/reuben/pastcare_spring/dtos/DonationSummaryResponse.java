package com.reuben.pastcare_spring.dtos;

import java.math.BigDecimal;

/**
 * Giving Module Phase 1: Donation Recording
 * Summary statistics for donations
 */
public record DonationSummaryResponse(
    BigDecimal totalAmount,
    Long donationCount,
    BigDecimal averageDonation,
    BigDecimal largestDonation,
    BigDecimal smallestDonation,
    Long uniqueDonors,
    Long anonymousDonations
) {
}
