package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.CongregationPricingTier;
import com.reuben.pastcare_spring.models.SubscriptionBillingInterval;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import com.reuben.pastcare_spring.repositories.CongregationPricingTierRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.SubscriptionBillingIntervalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing congregation-based pricing tiers.
 *
 * <p>Core business logic for:
 * <ul>
 *   <li>Determining appropriate tier based on member count</li>
 *   <li>Calculating pricing for different billing intervals</li>
 *   <li>Detecting when tier upgrade is required</li>
 *   <li>Managing tier assignments for churches</li>
 * </ul>
 *
 * @since 2026-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CongregationPricingService {

    private final CongregationPricingTierRepository tierRepository;
    private final ChurchRepository churchRepository;
    private final ChurchSubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionBillingIntervalRepository billingIntervalRepository;

    /**
     * Get all active pricing tiers ordered by display order.
     *
     * @return List of active tiers
     */
    public List<CongregationPricingTier> getAllActiveTiers() {
        return tierRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Get a specific pricing tier by ID.
     *
     * @param tierId Tier ID
     * @return Pricing tier
     * @throws IllegalArgumentException if tier not found
     */
    public CongregationPricingTier getTierById(Long tierId) {
        return tierRepository.findById(tierId)
                .orElseThrow(() -> new IllegalArgumentException("Pricing tier not found: " + tierId));
    }

    /**
     * Get a pricing tier by name.
     *
     * @param tierName Tier name (e.g., "TIER_1", "TIER_2")
     * @return Pricing tier
     * @throws IllegalArgumentException if tier not found
     */
    public CongregationPricingTier getTierByName(String tierName) {
        return tierRepository.findByTierName(tierName)
                .orElseThrow(() -> new IllegalArgumentException("Pricing tier not found: " + tierName));
    }

    /**
     * Determine the appropriate pricing tier for a given member count.
     *
     * <p>Returns the tier whose range includes the member count.
     * For example: 250 members → TIER_2 (201-500)
     *
     * @param memberCount Number of members in congregation
     * @return Appropriate pricing tier
     * @throws IllegalStateException if no tier matches (should never happen with proper tier setup)
     */
    public CongregationPricingTier getPricingTierForMemberCount(int memberCount) {
        if (memberCount < 1) {
            throw new IllegalArgumentException("Member count must be positive");
        }

        Optional<CongregationPricingTier> tier = tierRepository.findTierForMemberCount(memberCount);

        if (tier.isEmpty()) {
            log.error("No pricing tier found for member count: {}. Check tier configuration.", memberCount);
            throw new IllegalStateException(
                    "No pricing tier available for " + memberCount + " members. " +
                    "Please contact support.");
        }

        return tier.get();
    }

    /**
     * Get the current pricing tier for a church based on its subscription.
     *
     * @param churchId Church ID
     * @return Current pricing tier
     * @throws IllegalArgumentException if church not found or no subscription
     */
    public CongregationPricingTier getCurrentTierForChurch(Long churchId) {
        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No subscription found for church: " + churchId));

        if (subscription.getPricingTier() == null) {
            throw new IllegalStateException(
                    "Church subscription has no pricing tier assigned. Migration may be incomplete.");
        }

        return subscription.getPricingTier();
    }

    /**
     * Get the recommended pricing tier for a church based on current member count.
     *
     * <p>This may differ from current tier if church has grown.
     *
     * @param churchId Church ID
     * @return Recommended pricing tier
     */
    @Transactional(readOnly = true)
    public CongregationPricingTier getRecommendedTierForChurch(Long churchId) {
        // Get current member count
        int memberCount = (int) memberRepository.countByChurchId(churchId);

        // Get tier for this member count
        return getPricingTierForMemberCount(memberCount);
    }

    /**
     * Calculate the price for a church at a specific billing interval.
     *
     * <p>Uses the church's current pricing tier and requested billing interval.
     *
     * @param churchId Church ID
     * @param intervalName Billing interval (MONTHLY, QUARTERLY, BIANNUAL, ANNUAL)
     * @return Price in USD for the billing interval
     * @throws IllegalArgumentException if church or interval not found
     */
    @Transactional(readOnly = true)
    public BigDecimal calculatePriceForChurch(Long churchId, String intervalName) {
        CongregationPricingTier tier = getCurrentTierForChurch(churchId);
        return tier.getPriceForInterval(intervalName);
    }

    /**
     * Calculate price for a specific tier and billing interval.
     *
     * <p>Used for pricing display before church signs up.
     *
     * @param tierName Tier name (e.g., "TIER_1")
     * @param intervalName Billing interval
     * @return Price in USD
     */
    public BigDecimal calculatePrice(String tierName, String intervalName) {
        CongregationPricingTier tier = getTierByName(tierName);
        return tier.getPriceForInterval(intervalName);
    }

    /**
     * Calculate price for a member count and billing interval.
     *
     * <p>Determines appropriate tier automatically based on member count.
     *
     * @param memberCount Number of members
     * @param intervalName Billing interval
     * @return Price in USD
     */
    public BigDecimal calculatePriceForMemberCount(int memberCount, String intervalName) {
        CongregationPricingTier tier = getPricingTierForMemberCount(memberCount);
        return tier.getPriceForInterval(intervalName);
    }

    /**
     * Check if a church needs to upgrade its tier.
     *
     * <p>Returns true if:
     * <ul>
     *   <li>Church's member count exceeds current tier's maximum</li>
     *   <li>OR church is approaching the limit (>95%)</li>
     * </ul>
     *
     * @param churchId Church ID
     * @return true if upgrade is recommended
     */
    @Transactional(readOnly = true)
    public boolean checkTierUpgradeRequired(Long churchId) {
        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription found"));

        CongregationPricingTier currentTier = subscription.getPricingTier();
        if (currentTier == null) {
            return false;
        }

        // Unlimited tier never needs upgrade
        if (currentTier.getMaxMembers() == null) {
            return false;
        }

        int currentMemberCount = (int) memberRepository.countByChurchId(churchId);

        // Check if exceeded tier max
        if (currentMemberCount > currentTier.getMaxMembers()) {
            return true;
        }

        // Check if approaching limit (>95%)
        double percentageUsed = (currentMemberCount * 100.0) / currentTier.getMaxMembers();
        return percentageUsed > 95.0;
    }

    /**
     * Get the next higher tier for upgrade purposes.
     *
     * <p>Returns the tier with the next higher display order.
     *
     * @param currentTier Current tier
     * @return Next tier, or null if already on highest tier
     */
    public CongregationPricingTier getNextTier(CongregationPricingTier currentTier) {
        List<CongregationPricingTier> allTiers = getAllActiveTiers();

        for (int i = 0; i < allTiers.size() - 1; i++) {
            if (allTiers.get(i).getId().equals(currentTier.getId())) {
                return allTiers.get(i + 1);
            }
        }

        return null; // Already on highest tier
    }

    /**
     * Get tier upgrade information for a church.
     *
     * @param churchId Church ID
     * @return TierUpgradeInfo with details
     */
    @Transactional(readOnly = true)
    public TierUpgradeInfo getTierUpgradeInfo(Long churchId) {
        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription found"));

        CongregationPricingTier currentTier = subscription.getPricingTier();
        int currentMemberCount = (int) memberRepository.countByChurchId(churchId);

        boolean upgradeRequired = checkTierUpgradeRequired(churchId);
        CongregationPricingTier recommendedTier = upgradeRequired
                ? getPricingTierForMemberCount(currentMemberCount)
                : currentTier;

        CongregationPricingTier nextTier = getNextTier(currentTier);

        return new TierUpgradeInfo(
                currentTier,
                recommendedTier,
                nextTier,
                currentMemberCount,
                upgradeRequired
        );
    }

    /**
     * Assign a pricing tier to a church.
     *
     * <p>Updates the church's subscription with the new tier.
     * Does NOT change billing or payment - that's handled by BillingService.
     *
     * @param churchId Church ID
     * @param tierId Tier ID to assign
     * @param billingInterval Billing interval
     * @return Updated subscription
     */
    @Transactional
    public ChurchSubscription assignTierToChurch(
            Long churchId,
            Long tierId,
            SubscriptionBillingInterval billingInterval) {

        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Church not found: " + churchId));

        CongregationPricingTier tier = getTierById(tierId);

        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription found"));

        // Verify member count is within tier limits
        int memberCount = (int) memberRepository.countByChurchId(churchId);
        if (!tier.isInRange(memberCount)) {
            throw new IllegalArgumentException(
                    String.format("Church has %d members but selected tier (%s) is for %d-%s members",
                            memberCount,
                            tier.getDisplayName(),
                            tier.getMinMembers(),
                            tier.getMaxMembers() == null ? "∞" : tier.getMaxMembers().toString())
            );
        }

        // Update subscription
        subscription.setPricingTier(tier);
        subscription.setBillingInterval(billingInterval);
        subscription.setCurrentMemberCount(memberCount);
        subscription.setMemberCountLastChecked(java.time.LocalDateTime.now());

        // Calculate and set subscription amount
        BigDecimal amount = tier.getPriceForInterval(billingInterval.getIntervalName());
        subscription.setSubscriptionAmount(amount);

        // Update church's eligible tier
        church.setEligiblePricingTierId(tierId);
        church.setCachedMemberCount(memberCount);
        church.setMemberCountLastUpdated(java.time.LocalDateTime.now());

        churchRepository.save(church);

        log.info("Assigned tier {} ({}) to church {} with {} members. Price: ${}/{}",
                tier.getTierName(),
                tier.getDisplayName(),
                churchId,
                memberCount,
                amount,
                billingInterval.getIntervalName());

        return subscriptionRepository.save(subscription);
    }

    /**
     * Get all billing intervals.
     *
     * @return List of all active billing intervals
     */
    public List<SubscriptionBillingInterval> getAllBillingIntervals() {
        return billingIntervalRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Get detailed information about a church's current tier and member count.
     *
     * @param churchId Church ID
     * @return Church tier information
     */
    @Transactional(readOnly = true)
    public ChurchTierInfo getChurchTierInfo(Long churchId) {
        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription found"));

        CongregationPricingTier currentTier = subscription.getPricingTier();
        if (currentTier == null) {
            throw new IllegalStateException("Church subscription has no pricing tier assigned");
        }

        int currentMemberCount = (int) memberRepository.countByChurchId(churchId);

        Integer tierMaxMembers = currentTier.getMaxMembers();
        double percentageUsed = 0.0;
        Integer membersRemaining = null;

        if (tierMaxMembers != null) {
            percentageUsed = (currentMemberCount * 100.0) / tierMaxMembers;
            membersRemaining = tierMaxMembers - currentMemberCount;
        }

        boolean approachingLimit = tierMaxMembers != null && percentageUsed > 80.0;
        boolean exceededLimit = tierMaxMembers != null && currentMemberCount > tierMaxMembers;

        return new ChurchTierInfo(
                currentTier,
                currentMemberCount,
                tierMaxMembers,
                percentageUsed,
                membersRemaining,
                approachingLimit,
                exceededLimit
        );
    }

    /**
     * Create a new pricing tier (SUPERADMIN only).
     *
     * @param tierName Tier name (e.g., "TIER_6")
     * @param displayName Display name (e.g., "Mega Church (5001+)")
     * @param description Tier description
     * @param minMembers Minimum members for this tier
     * @param maxMembers Maximum members (null for unlimited)
     * @param monthlyPriceUsd Monthly price in USD
     * @param quarterlyPriceUsd Quarterly price in USD
     * @param biannualPriceUsd Biannual price in USD
     * @param annualPriceUsd Annual price in USD
     * @param features JSON string of features
     * @param displayOrder Display order
     * @return Created tier
     */
    @Transactional
    public CongregationPricingTier createTier(
            String tierName,
            String displayName,
            String description,
            int minMembers,
            Integer maxMembers,
            BigDecimal monthlyPriceUsd,
            BigDecimal quarterlyPriceUsd,
            BigDecimal biannualPriceUsd,
            BigDecimal annualPriceUsd,
            String features,
            int displayOrder) {

        // Validate tier doesn't already exist
        if (tierRepository.findByTierName(tierName).isPresent()) {
            throw new IllegalArgumentException("Tier with name " + tierName + " already exists");
        }

        // Calculate discount percentages
        BigDecimal monthlyTotal3 = monthlyPriceUsd.multiply(BigDecimal.valueOf(3));
        BigDecimal quarterlyDiscount = monthlyTotal3.subtract(quarterlyPriceUsd)
                .divide(monthlyTotal3, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        BigDecimal monthlyTotal6 = monthlyPriceUsd.multiply(BigDecimal.valueOf(6));
        BigDecimal biannualDiscount = monthlyTotal6.subtract(biannualPriceUsd)
                .divide(monthlyTotal6, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        BigDecimal monthlyTotal12 = monthlyPriceUsd.multiply(BigDecimal.valueOf(12));
        BigDecimal annualDiscount = monthlyTotal12.subtract(annualPriceUsd)
                .divide(monthlyTotal12, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        CongregationPricingTier tier = CongregationPricingTier.builder()
                .tierName(tierName)
                .displayName(displayName)
                .description(description)
                .minMembers(minMembers)
                .maxMembers(maxMembers)
                .monthlyPriceUsd(monthlyPriceUsd)
                .quarterlyPriceUsd(quarterlyPriceUsd)
                .biannualPriceUsd(biannualPriceUsd)
                .annualPriceUsd(annualPriceUsd)
                .quarterlyDiscountPct(quarterlyDiscount)
                .biannualDiscountPct(biannualDiscount)
                .annualDiscountPct(annualDiscount)
                .features(features)
                .isActive(true)
                .displayOrder(displayOrder)
                .build();

        log.info("Creating new pricing tier: {} ({}) - ${}/month for {}-{} members",
                tierName, displayName, monthlyPriceUsd, minMembers,
                maxMembers == null ? "∞" : maxMembers);

        return tierRepository.save(tier);
    }

    /**
     * Update an existing pricing tier (SUPERADMIN only).
     *
     * @param tierId Tier ID
     * @param displayName New display name (null to keep existing)
     * @param description New description (null to keep existing)
     * @param monthlyPriceUsd New monthly price (null to keep existing)
     * @param quarterlyPriceUsd New quarterly price (null to keep existing)
     * @param biannualPriceUsd New biannual price (null to keep existing)
     * @param annualPriceUsd New annual price (null to keep existing)
     * @param features New features JSON (null to keep existing)
     * @param displayOrder New display order (null to keep existing)
     * @return Updated tier
     */
    @Transactional
    public CongregationPricingTier updateTier(
            Long tierId,
            String displayName,
            String description,
            BigDecimal monthlyPriceUsd,
            BigDecimal quarterlyPriceUsd,
            BigDecimal biannualPriceUsd,
            BigDecimal annualPriceUsd,
            String features,
            Integer displayOrder) {

        CongregationPricingTier tier = getTierById(tierId);

        if (displayName != null) tier.setDisplayName(displayName);
        if (description != null) tier.setDescription(description);
        if (features != null) tier.setFeatures(features);
        if (displayOrder != null) tier.setDisplayOrder(displayOrder);

        // Update prices and recalculate discounts if any price changed
        boolean pricesChanged = false;

        if (monthlyPriceUsd != null) {
            tier.setMonthlyPriceUsd(monthlyPriceUsd);
            pricesChanged = true;
        }
        if (quarterlyPriceUsd != null) {
            tier.setQuarterlyPriceUsd(quarterlyPriceUsd);
            pricesChanged = true;
        }
        if (biannualPriceUsd != null) {
            tier.setBiannualPriceUsd(biannualPriceUsd);
            pricesChanged = true;
        }
        if (annualPriceUsd != null) {
            tier.setAnnualPriceUsd(annualPriceUsd);
            pricesChanged = true;
        }

        if (pricesChanged) {
            // Recalculate discount percentages
            BigDecimal monthly = tier.getMonthlyPriceUsd();

            BigDecimal monthlyTotal3 = monthly.multiply(BigDecimal.valueOf(3));
            BigDecimal quarterlyDiscount = monthlyTotal3.subtract(tier.getQuarterlyPriceUsd())
                    .divide(monthlyTotal3, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            tier.setQuarterlyDiscountPct(quarterlyDiscount);

            BigDecimal monthlyTotal6 = monthly.multiply(BigDecimal.valueOf(6));
            BigDecimal biannualDiscount = monthlyTotal6.subtract(tier.getBiannualPriceUsd())
                    .divide(monthlyTotal6, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            tier.setBiannualDiscountPct(biannualDiscount);

            BigDecimal monthlyTotal12 = monthly.multiply(BigDecimal.valueOf(12));
            BigDecimal annualDiscount = monthlyTotal12.subtract(tier.getAnnualPriceUsd())
                    .divide(monthlyTotal12, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            tier.setAnnualDiscountPct(annualDiscount);
        }

        tier.setUpdatedAt(LocalDateTime.now());

        log.info("Updated pricing tier {}: {}", tierId, tier.getTierName());

        return tierRepository.save(tier);
    }

    /**
     * Deactivate a pricing tier (SUPERADMIN only).
     *
     * <p>Does not delete the tier, just marks it as inactive.
     * Churches already on this tier will remain on it, but new signups won't see it.
     *
     * @param tierId Tier ID to deactivate
     */
    @Transactional
    public void deactivateTier(Long tierId) {
        CongregationPricingTier tier = getTierById(tierId);
        tier.setIsActive(false);
        tier.setUpdatedAt(LocalDateTime.now());
        tierRepository.save(tier);

        log.info("Deactivated pricing tier {}: {}", tierId, tier.getTierName());
    }

    /**
     * Information about tier upgrade for a church.
     */
    public record TierUpgradeInfo(
            CongregationPricingTier currentTier,
            CongregationPricingTier recommendedTier,
            CongregationPricingTier nextTier,
            int currentMemberCount,
            boolean upgradeRequired
    ) {
        public boolean isOnRecommendedTier() {
            return currentTier.getId().equals(recommendedTier.getId());
        }

        public boolean hasNextTier() {
            return nextTier != null;
        }

        public String getUpgradeMessage() {
            if (!upgradeRequired) {
                return "Your current tier is appropriate for your member count.";
            }

            if (currentTier.getMaxMembers() == null) {
                return "You're on the unlimited Enterprise tier.";
            }

            return String.format(
                    "Your church has %d members, which exceeds your current tier's limit of %d members. " +
                    "We recommend upgrading to %s tier.",
                    currentMemberCount,
                    currentTier.getMaxMembers(),
                    recommendedTier.getDisplayName()
            );
        }
    }

    /**
     * Detailed information about a church's current tier status.
     */
    public record ChurchTierInfo(
            CongregationPricingTier currentTier,
            int currentMemberCount,
            Integer tierMaxMembers,
            double percentageUsed,
            Integer membersRemaining,
            boolean approachingLimit,
            boolean exceededLimit
    ) {
        public String getStatusMessage() {
            if (tierMaxMembers == null) {
                return "You're on an unlimited tier - no member restrictions.";
            }

            if (exceededLimit) {
                return String.format("URGENT: You have %d members but your tier allows up to %d. Please upgrade immediately.",
                        currentMemberCount, tierMaxMembers);
            }

            if (approachingLimit) {
                return String.format("Warning: You're using %.1f%% of your tier limit (%d/%d members). Consider upgrading soon.",
                        percentageUsed, currentMemberCount, tierMaxMembers);
            }

            return String.format("You have %d members (%.1f%% of your %d member limit).",
                    currentMemberCount, percentageUsed, tierMaxMembers);
        }
    }
}
