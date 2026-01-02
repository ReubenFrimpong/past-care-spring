package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dto.ProrationCalculation;
import com.reuben.pastcare_spring.exceptions.ResourceNotFoundException;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.CongregationPricingTier;
import com.reuben.pastcare_spring.models.SubscriptionBillingInterval;
import com.reuben.pastcare_spring.repositories.CongregationPricingTierRepository;
import com.reuben.pastcare_spring.repositories.SubscriptionBillingIntervalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Core engine for calculating prorated billing for subscription tier upgrades.
 *
 * <p>This service handles three types of upgrades:
 * <ul>
 *   <li><b>Tier-only upgrade</b>: Change tier, keep billing interval (e.g., TIER_2 monthly → TIER_3 monthly)</li>
 *   <li><b>Interval-only change</b>: Change billing interval, keep tier (e.g., TIER_2 monthly → TIER_2 annual)</li>
 *   <li><b>Combined upgrade</b>: Change both tier and interval (e.g., TIER_2 monthly → TIER_3 annual)</li>
 * </ul>
 *
 * <p><b>Proration Formula:</b>
 * <pre>
 * Credit for unused time = (old daily rate) × (days remaining)
 * Prorated charge for new tier = (new daily rate) × (days remaining)
 * Net charge = prorated charge - unused credit
 * </pre>
 *
 * <p><b>Special Cases:</b>
 * <ul>
 *   <li>Tier-only: Next billing date stays the same</li>
 *   <li>Interval change: Charges full new interval price, next billing date extends</li>
 *   <li>Combined: Charges full new interval+tier price, next billing date extends</li>
 * </ul>
 *
 * @since 2026-01-02
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProrationCalculationService {

    private final CongregationPricingTierRepository tierRepository;
    private final SubscriptionBillingIntervalRepository intervalRepository;
    private final CurrencyConversionService currencyConversion;

    /**
     * Main entry point - calculates proration for tier/interval upgrade.
     *
     * <p>Automatically detects upgrade type and routes to appropriate calculation method.
     *
     * @param subscription Current subscription
     * @param newTierId Target tier ID (required)
     * @param newIntervalId Target interval ID (null for tier-only upgrade)
     * @return Complete proration calculation with all financial details
     * @throws ResourceNotFoundException if tier or interval not found
     * @throws IllegalArgumentException if no changes detected
     */
    public ProrationCalculation calculateUpgrade(
            ChurchSubscription subscription,
            Long newTierId,
            Long newIntervalId) {

        CongregationPricingTier currentTier = subscription.getPricingTier();
        SubscriptionBillingInterval currentInterval = subscription.getBillingInterval();

        // Fetch new tier
        CongregationPricingTier newTier = tierRepository.findById(newTierId)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + newTierId));

        // Fetch new interval (optional)
        SubscriptionBillingInterval newInterval = null;
        if (newIntervalId != null) {
            newInterval = intervalRepository.findById(newIntervalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Billing interval not found: " + newIntervalId));
        }

        // Determine change type
        boolean tierChanged = !currentTier.getId().equals(newTier.getId());
        boolean intervalChanged = newInterval != null && !currentInterval.getId().equals(newInterval.getId());

        String changeType;
        if (tierChanged && intervalChanged) {
            changeType = "COMBINED";
        } else if (tierChanged) {
            changeType = "TIER_UPGRADE";
        } else if (intervalChanged) {
            changeType = "INTERVAL_CHANGE";
        } else {
            throw new IllegalArgumentException("No changes detected in tier or interval - upgrade not needed");
        }

        log.info("Calculating {} upgrade for subscription {}: {} → {}",
                changeType,
                subscription.getId(),
                currentTier.getTierName() + "/" + currentInterval.getIntervalName(),
                newTier.getTierName() + "/" + (newInterval != null ? newInterval.getIntervalName() : currentInterval.getIntervalName()));

        // Route to appropriate calculation method
        return switch (changeType) {
            case "TIER_UPGRADE" -> calculateTierOnlyUpgrade(subscription, newTier);
            case "INTERVAL_CHANGE" -> calculateIntervalOnlyChange(subscription, newInterval);
            case "COMBINED" -> calculateCombinedUpgrade(subscription, newTier, newInterval);
            default -> throw new IllegalStateException("Unexpected change type: " + changeType);
        };
    }

    /**
     * Calculate tier-only upgrade (same billing interval).
     *
     * <p><b>Example:</b> TIER_2 monthly → TIER_3 monthly, 15 days remaining
     * <pre>
     * Old price: $9.99/month (30 days)
     * New price: $13.99/month (30 days)
     * Unused credit: $9.99 × (15/30) = $5.00
     * Prorated charge: $13.99 × (15/30) = $7.00
     * Net charge: $7.00 - $5.00 = $2.00
     * Next billing date: STAYS THE SAME
     * </pre>
     *
     * @param subscription Current subscription
     * @param newTier Target tier
     * @return Proration calculation
     */
    private ProrationCalculation calculateTierOnlyUpgrade(
            ChurchSubscription subscription,
            CongregationPricingTier newTier) {

        SubscriptionBillingInterval interval = subscription.getBillingInterval();

        // Get prices for current interval
        BigDecimal oldPriceUsd = subscription.getPricingTier()
                .getPriceForInterval(interval.getIntervalName());
        BigDecimal newPriceUsd = newTier.getPriceForInterval(interval.getIntervalName());

        // Calculate days
        LocalDate today = LocalDate.now();
        LocalDate nextBillingDate = subscription.getNextBillingDate();
        int daysRemaining = (int) ChronoUnit.DAYS.between(today, nextBillingDate);

        // Edge case: ensure at least 1 day
        if (daysRemaining <= 0) {
            daysRemaining = 1;
            log.warn("Days remaining was {} for subscription {}, set to 1 for safety",
                    daysRemaining, subscription.getId());
        }

        int totalDaysInPeriod = interval.getMonths() * 30; // Approximate (30 days per month)
        int daysUsed = totalDaysInPeriod - daysRemaining;

        // Calculate daily rates (4 decimal places for precision)
        BigDecimal oldDailyRateUsd = oldPriceUsd.divide(
                new BigDecimal(totalDaysInPeriod), 4, RoundingMode.HALF_UP);
        BigDecimal newDailyRateUsd = newPriceUsd.divide(
                new BigDecimal(totalDaysInPeriod), 4, RoundingMode.HALF_UP);

        // Calculate proration (USD)
        BigDecimal unusedCreditUsd = oldDailyRateUsd.multiply(new BigDecimal(daysRemaining))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal proratedChargeUsd = newDailyRateUsd.multiply(new BigDecimal(daysRemaining))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal netChargeUsd = proratedChargeUsd.subtract(unusedCreditUsd)
                .setScale(2, RoundingMode.HALF_UP);

        // Ensure non-negative (edge case: downgrade might give negative, but we don't support downgrades)
        if (netChargeUsd.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Net charge was negative: {} for subscription {}. Setting to 0.",
                    netChargeUsd, subscription.getId());
            netChargeUsd = BigDecimal.ZERO;
        }

        // Convert to GHS (payment currency)
        BigDecimal oldPriceGhs = currencyConversion.convertUsdToGhs(oldPriceUsd);
        BigDecimal newPriceGhs = currencyConversion.convertUsdToGhs(newPriceUsd);
        BigDecimal unusedCreditGhs = currencyConversion.convertUsdToGhs(unusedCreditUsd);
        BigDecimal proratedChargeGhs = currencyConversion.convertUsdToGhs(proratedChargeUsd);
        BigDecimal netChargeGhs = currencyConversion.convertUsdToGhs(netChargeUsd);

        log.info("Tier-only upgrade calculation: {} days remaining, credit GHS {}, charge GHS {}, net GHS {}",
                daysRemaining, unusedCreditGhs, proratedChargeGhs, netChargeGhs);

        ProrationCalculation calculation = new ProrationCalculation(
                daysRemaining, daysUsed, totalDaysInPeriod,
                oldPriceUsd, oldPriceGhs,
                newPriceUsd, newPriceGhs,
                unusedCreditUsd, proratedChargeUsd, netChargeUsd,
                unusedCreditGhs, proratedChargeGhs, netChargeGhs,
                nextBillingDate, // SAME next billing date
                nextBillingDate,
                "TIER_UPGRADE",
                subscription.getPricingTier().getTierName(),
                newTier.getTierName(),
                interval.getIntervalName(),
                interval.getIntervalName()
        );

        calculation.validate();
        return calculation;
    }

    /**
     * Calculate interval-only change (same tier).
     *
     * <p><b>Example:</b> TIER_2 monthly → TIER_2 annual, 15 days remaining
     * <pre>
     * Old price: $9.99/month (30 days)
     * New price: $107.88/year
     * Unused credit: $9.99 × (15/30) = $5.00
     * Prorated charge: $107.88 (FULL annual price, not prorated)
     * Net charge: $107.88 - $5.00 = $102.88
     * Next billing date: current date + 12 months
     * </pre>
     *
     * @param subscription Current subscription
     * @param newInterval Target interval
     * @return Proration calculation
     */
    private ProrationCalculation calculateIntervalOnlyChange(
            ChurchSubscription subscription,
            SubscriptionBillingInterval newInterval) {

        CongregationPricingTier tier = subscription.getPricingTier();
        SubscriptionBillingInterval oldInterval = subscription.getBillingInterval();

        // Get prices
        BigDecimal oldPriceUsd = tier.getPriceForInterval(oldInterval.getIntervalName());
        BigDecimal newPriceUsd = tier.getPriceForInterval(newInterval.getIntervalName());

        // Calculate days
        LocalDate today = LocalDate.now();
        LocalDate currentNextBillingDate = subscription.getNextBillingDate();
        int daysRemaining = (int) ChronoUnit.DAYS.between(today, currentNextBillingDate);

        if (daysRemaining <= 0) {
            daysRemaining = 1;
            log.warn("Days remaining was {} for subscription {}, set to 1", daysRemaining, subscription.getId());
        }

        int totalDaysInOldPeriod = oldInterval.getMonths() * 30;
        int daysUsed = totalDaysInOldPeriod - daysRemaining;

        // Calculate unused credit from old interval
        BigDecimal oldDailyRateUsd = oldPriceUsd.divide(
                new BigDecimal(totalDaysInOldPeriod), 4, RoundingMode.HALF_UP);
        BigDecimal unusedCreditUsd = oldDailyRateUsd.multiply(new BigDecimal(daysRemaining))
                .setScale(2, RoundingMode.HALF_UP);

        // Charge FULL new interval price (not prorated)
        BigDecimal proratedChargeUsd = newPriceUsd;

        // Net charge = full new price - credit
        BigDecimal netChargeUsd = proratedChargeUsd.subtract(unusedCreditUsd)
                .setScale(2, RoundingMode.HALF_UP);

        if (netChargeUsd.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Net charge was negative: {} for subscription {}. Setting to 0.",
                    netChargeUsd, subscription.getId());
            netChargeUsd = BigDecimal.ZERO;
        }

        // New billing date extends from current next billing date
        LocalDate newNextBillingDate = currentNextBillingDate.plusMonths(newInterval.getMonths());

        // Convert to GHS
        BigDecimal oldPriceGhs = currencyConversion.convertUsdToGhs(oldPriceUsd);
        BigDecimal newPriceGhs = currencyConversion.convertUsdToGhs(newPriceUsd);
        BigDecimal unusedCreditGhs = currencyConversion.convertUsdToGhs(unusedCreditUsd);
        BigDecimal proratedChargeGhs = currencyConversion.convertUsdToGhs(proratedChargeUsd);
        BigDecimal netChargeGhs = currencyConversion.convertUsdToGhs(netChargeUsd);

        log.info("Interval change calculation: {} → {}, {} days remaining, credit GHS {}, charge GHS {}, net GHS {}",
                oldInterval.getIntervalName(), newInterval.getIntervalName(),
                daysRemaining, unusedCreditGhs, proratedChargeGhs, netChargeGhs);

        ProrationCalculation calculation = new ProrationCalculation(
                daysRemaining, daysUsed, totalDaysInOldPeriod,
                oldPriceUsd, oldPriceGhs,
                newPriceUsd, newPriceGhs,
                unusedCreditUsd, proratedChargeUsd, netChargeUsd,
                unusedCreditGhs, proratedChargeGhs, netChargeGhs,
                currentNextBillingDate,
                newNextBillingDate, // EXTENDED by new interval
                "INTERVAL_CHANGE",
                tier.getTierName(),
                tier.getTierName(),
                oldInterval.getIntervalName(),
                newInterval.getIntervalName()
        );

        calculation.validate();
        return calculation;
    }

    /**
     * Calculate combined tier + interval upgrade.
     *
     * <p><b>Example:</b> TIER_2 monthly → TIER_3 annual, 15 days remaining
     * <pre>
     * Old price: $9.99/month (30 days)
     * New price: $151.88/year (TIER_3 annual)
     * Unused credit: $9.99 × (15/30) = $5.00
     * Prorated charge: $151.88 (FULL annual price for new tier)
     * Net charge: $151.88 - $5.00 = $146.88
     * Next billing date: current date + 12 months
     * </pre>
     *
     * @param subscription Current subscription
     * @param newTier Target tier
     * @param newInterval Target interval
     * @return Proration calculation
     */
    private ProrationCalculation calculateCombinedUpgrade(
            ChurchSubscription subscription,
            CongregationPricingTier newTier,
            SubscriptionBillingInterval newInterval) {

        CongregationPricingTier oldTier = subscription.getPricingTier();
        SubscriptionBillingInterval oldInterval = subscription.getBillingInterval();

        // Get prices
        BigDecimal oldPriceUsd = oldTier.getPriceForInterval(oldInterval.getIntervalName());
        BigDecimal newPriceUsd = newTier.getPriceForInterval(newInterval.getIntervalName());

        // Calculate days
        LocalDate today = LocalDate.now();
        LocalDate currentNextBillingDate = subscription.getNextBillingDate();
        int daysRemaining = (int) ChronoUnit.DAYS.between(today, currentNextBillingDate);

        if (daysRemaining <= 0) {
            daysRemaining = 1;
            log.warn("Days remaining was {} for subscription {}, set to 1", daysRemaining, subscription.getId());
        }

        int totalDaysInOldPeriod = oldInterval.getMonths() * 30;
        int daysUsed = totalDaysInOldPeriod - daysRemaining;

        // Calculate unused credit from old tier+interval
        BigDecimal oldDailyRateUsd = oldPriceUsd.divide(
                new BigDecimal(totalDaysInOldPeriod), 4, RoundingMode.HALF_UP);
        BigDecimal unusedCreditUsd = oldDailyRateUsd.multiply(new BigDecimal(daysRemaining))
                .setScale(2, RoundingMode.HALF_UP);

        // Charge FULL new tier+interval price
        BigDecimal proratedChargeUsd = newPriceUsd;

        // Net charge
        BigDecimal netChargeUsd = proratedChargeUsd.subtract(unusedCreditUsd)
                .setScale(2, RoundingMode.HALF_UP);

        if (netChargeUsd.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Net charge was negative: {} for subscription {}. Setting to 0.",
                    netChargeUsd, subscription.getId());
            netChargeUsd = BigDecimal.ZERO;
        }

        // New billing date extends from current next billing date
        LocalDate newNextBillingDate = currentNextBillingDate.plusMonths(newInterval.getMonths());

        // Convert to GHS
        BigDecimal oldPriceGhs = currencyConversion.convertUsdToGhs(oldPriceUsd);
        BigDecimal newPriceGhs = currencyConversion.convertUsdToGhs(newPriceUsd);
        BigDecimal unusedCreditGhs = currencyConversion.convertUsdToGhs(unusedCreditUsd);
        BigDecimal proratedChargeGhs = currencyConversion.convertUsdToGhs(proratedChargeUsd);
        BigDecimal netChargeGhs = currencyConversion.convertUsdToGhs(netChargeUsd);

        log.info("Combined upgrade calculation: {}/{} → {}/{}, {} days remaining, credit GHS {}, charge GHS {}, net GHS {}",
                oldTier.getTierName(), oldInterval.getIntervalName(),
                newTier.getTierName(), newInterval.getIntervalName(),
                daysRemaining, unusedCreditGhs, proratedChargeGhs, netChargeGhs);

        ProrationCalculation calculation = new ProrationCalculation(
                daysRemaining, daysUsed, totalDaysInOldPeriod,
                oldPriceUsd, oldPriceGhs,
                newPriceUsd, newPriceGhs,
                unusedCreditUsd, proratedChargeUsd, netChargeUsd,
                unusedCreditGhs, proratedChargeGhs, netChargeGhs,
                currentNextBillingDate,
                newNextBillingDate, // EXTENDED by new interval
                "COMBINED",
                oldTier.getTierName(),
                newTier.getTierName(),
                oldInterval.getIntervalName(),
                newInterval.getIntervalName()
        );

        calculation.validate();
        return calculation;
    }
}
