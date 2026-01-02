package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.exceptions.InvalidTierSelectionException;
import com.reuben.pastcare_spring.exceptions.ResourceNotFoundException;
import com.reuben.pastcare_spring.models.CongregationPricingTier;
import com.reuben.pastcare_spring.repositories.CongregationPricingTierRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for validating subscription tier selections based on church member count.
 *
 * <p>Ensures that churches can only select tiers that accommodate their current
 * number of members. This prevents revenue loss from churches selecting tiers
 * below their actual size.
 *
 * <p><b>Validation Rules:</b>
 * <ul>
 *   <li>Church with 150 members CAN select TIER_1 (1-200), TIER_2 (201-500), etc.</li>
 *   <li>Church with 350 members CANNOT select TIER_1 (max 200) - must select TIER_2 or higher</li>
 *   <li>Member count is checked in real-time from database (no caching)</li>
 *   <li>Validation applies to both new subscriptions and tier upgrades</li>
 * </ul>
 *
 * @since 2026-01-02
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TierValidationService {

    private final MemberRepository memberRepository;
    private final CongregationPricingTierRepository tierRepository;

    /**
     * Check if church can select a specific tier based on current member count.
     *
     * <p>Performs real-time member count check against tier's min/max range.
     *
     * @param churchId Church ID
     * @param tierId Target tier ID
     * @return true if tier is valid for church's member count, false otherwise
     * @throws ResourceNotFoundException if tier not found
     */
    public boolean canSelectTier(Long churchId, Long tierId) {
        long memberCount = memberRepository.countByChurchId(churchId);

        CongregationPricingTier tier = tierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + tierId));

        boolean canSelect = tier.isInRange((int) memberCount);

        log.debug("Tier validation for church {}: {} members, tier {} ({}-{}): {}",
                churchId, memberCount, tier.getTierName(),
                tier.getMinMembers(),
                tier.getMaxMembers() != null ? tier.getMaxMembers() : "unlimited",
                canSelect ? "ALLOWED" : "DENIED");

        return canSelect;
    }

    /**
     * Get list of all valid tiers for church based on current member count.
     *
     * <p>Returns only tiers that can accommodate the church's member count.
     * Useful for filtering tier selection dropdowns.
     *
     * @param churchId Church ID
     * @return List of valid tiers (smallest to largest)
     */
    public List<CongregationPricingTier> getValidTiersForChurch(Long churchId) {
        long memberCount = memberRepository.countByChurchId(churchId);

        List<CongregationPricingTier> allTiers = tierRepository.findAllByOrderByMinMembersAsc();

        List<CongregationPricingTier> validTiers = allTiers.stream()
                .filter(tier -> tier.isInRange((int) memberCount))
                .collect(Collectors.toList());

        log.info("Church {} ({} members) has {} valid tier options: {}",
                churchId, memberCount, validTiers.size(),
                validTiers.stream().map(CongregationPricingTier::getTierName).collect(Collectors.joining(", ")));

        return validTiers;
    }

    /**
     * Validate tier selection and throw exception if invalid.
     *
     * <p>Use this method in endpoints/services to enforce tier validation.
     * Throws descriptive exception with member count details.
     *
     * @param churchId Church ID
     * @param tierId Target tier ID
     * @throws InvalidTierSelectionException if tier cannot accommodate member count
     * @throws ResourceNotFoundException if tier not found
     */
    public void validateTierSelection(Long churchId, Long tierId) {
        long memberCount = memberRepository.countByChurchId(churchId);

        CongregationPricingTier tier = tierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + tierId));

        if (!tier.isInRange((int) memberCount)) {
            String maxMembersDisplay = tier.getMaxMembers() != null
                    ? String.valueOf(tier.getMaxMembers())
                    : "unlimited";

            String message = String.format(
                    "Cannot select %s (range: %d-%s members) - church has %d members. " +
                    "Please select a tier that accommodates your current member count.",
                    tier.getDisplayName(),
                    tier.getMinMembers(),
                    maxMembersDisplay,
                    memberCount
            );

            log.warn("Tier validation failed for church {}: {}", churchId, message);
            throw new InvalidTierSelectionException(message);
        }

        log.info("Tier validation passed: Church {} ({} members) can select {} ({}-{})",
                churchId, memberCount, tier.getTierName(),
                tier.getMinMembers(), tier.getMaxMembers() != null ? tier.getMaxMembers() : "unlimited");
    }

    /**
     * Get minimum required tier for church's current member count.
     *
     * <p>Returns the smallest tier that can accommodate the church.
     * Useful for determining if a tier upgrade is required.
     *
     * @param churchId Church ID
     * @return Minimum required tier
     * @throws IllegalStateException if no tier found for member count (should never happen)
     */
    public CongregationPricingTier getMinimumRequiredTier(Long churchId) {
        long memberCount = memberRepository.countByChurchId(churchId);

        List<CongregationPricingTier> allTiers = tierRepository.findAllByOrderByMinMembersAsc();

        CongregationPricingTier minTier = allTiers.stream()
                .filter(tier -> tier.isInRange((int) memberCount))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No tier found for member count: %d. This should never happen - check tier configuration.", memberCount)));

        log.debug("Minimum required tier for church {} ({} members): {}",
                churchId, memberCount, minTier.getTierName());

        return minTier;
    }

    /**
     * Check if church's current tier is valid for their member count.
     *
     * <p>Returns false if church has grown beyond their current tier's max members.
     * This indicates a tier upgrade is required.
     *
     * @param churchId Church ID
     * @param currentTierId Current tier ID
     * @return true if current tier is still valid, false if upgrade required
     */
    public boolean isCurrentTierValid(Long churchId, Long currentTierId) {
        return canSelectTier(churchId, currentTierId);
    }

    /**
     * Get upgrade recommendation if church has exceeded current tier.
     *
     * <p>Returns null if current tier is still valid.
     * Returns recommended tier if upgrade is needed.
     *
     * @param churchId Church ID
     * @param currentTierId Current tier ID
     * @return Recommended tier, or null if no upgrade needed
     */
    public CongregationPricingTier getUpgradeRecommendation(Long churchId, Long currentTierId) {
        long memberCount = memberRepository.countByChurchId(churchId);

        CongregationPricingTier currentTier = tierRepository.findById(currentTierId)
                .orElseThrow(() -> new ResourceNotFoundException("Current tier not found: " + currentTierId));

        // If current tier is still valid, no upgrade needed
        if (currentTier.isInRange((int) memberCount)) {
            log.debug("Church {} ({} members) is still within tier {} range - no upgrade needed",
                    churchId, memberCount, currentTier.getTierName());
            return null;
        }

        // Find next suitable tier
        CongregationPricingTier recommendedTier = getMinimumRequiredTier(churchId);

        log.info("Upgrade recommended for church {} ({} members): {} → {}",
                churchId, memberCount, currentTier.getTierName(), recommendedTier.getTierName());

        return recommendedTier;
    }

    /**
     * Get member count statistics for a church.
     *
     * @param churchId Church ID
     * @return Member count statistics
     */
    public MemberCountStats getMemberCountStats(Long churchId) {
        long memberCount = memberRepository.countByChurchId(churchId);
        CongregationPricingTier minTier = getMinimumRequiredTier(churchId);
        List<CongregationPricingTier> validTiers = getValidTiersForChurch(churchId);

        return new MemberCountStats(
                (int) memberCount,
                minTier,
                validTiers.size(),
                validTiers
        );
    }

    /**
     * Member count statistics record.
     *
     * @param memberCount Current member count
     * @param minimumRequiredTier Smallest tier that accommodates members
     * @param validTierCount Number of tiers church can select
     * @param validTiers List of all valid tiers
     */
    public record MemberCountStats(
            int memberCount,
            CongregationPricingTier minimumRequiredTier,
            int validTierCount,
            List<CongregationPricingTier> validTiers
    ) {
        public boolean canSelectMultipleTiers() {
            return validTierCount > 1;
        }

        public String getMinimumTierName() {
            return minimumRequiredTier != null ? minimumRequiredTier.getTierName() : "UNKNOWN";
        }
    }
}
