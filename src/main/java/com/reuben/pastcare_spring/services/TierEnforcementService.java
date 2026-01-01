package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.exceptions.TierLimitExceededException;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.CongregationPricingTier;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for enforcing congregation size limits based on pricing tiers.
 *
 * <p>Prevents churches from exceeding their tier's maximum member count
 * through bulk uploads, individual member creation, or any other means.
 *
 * <p><strong>CRITICAL SECURITY:</strong> This service must be called BEFORE any
 * member creation operation (individual or bulk) to prevent tier bypass.
 *
 * @since 2026-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TierEnforcementService {

    private final ChurchRepository churchRepository;
    private final ChurchSubscriptionRepository churchSubscriptionRepository;
    private final MemberRepository memberRepository;

    /**
     * Technical buffer for minor discrepancies (e.g., concurrent requests).
     * Allows a small overage (1% of tier max) to prevent false positives.
     */
    private static final double BUFFER_PERCENTAGE = 0.01; // 1%

    /**
     * Check if church can add a specific number of new members.
     *
     * <p><strong>HARD BLOCK:</strong> Throws exception if adding members would exceed tier limit.
     *
     * <p>This method MUST be called in a transaction before committing member additions.
     *
     * @param churchId Church attempting to add members
     * @param membersToAdd Number of members to add (1 for individual, N for bulk)
     * @return TierCheckResult with allowed flag and detailed metrics
     * @throws TierLimitExceededException if tier limit would be exceeded
     */
    @Transactional(readOnly = true)
    public TierCheckResult canAddMembers(Long churchId, int membersToAdd) {
        // Get church and current member count
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalStateException("Church not found: " + churchId));

        // Get current subscription and pricing tier
        ChurchSubscription subscription = churchSubscriptionRepository.findActiveByChurchId(churchId)
                .orElseThrow(() -> new IllegalStateException(
                        "No active subscription found for church: " + churchId));

        CongregationPricingTier tier = subscription.getPricingTier();
        if (tier == null) {
            log.warn("Church {} has no pricing tier assigned. Allowing member addition.", churchId);
            // Fallback: allow if no tier (shouldn't happen in production)
            return new TierCheckResult(
                    true,
                    0,
                    null,
                    membersToAdd,
                    membersToAdd,
                    0.0,
                    "No tier assigned - allowed by default"
            );
        }

        // Get current actual member count from database (most accurate)
        int currentMemberCount = (int) memberRepository.countByChurchId(churchId);

        // Calculate new total after addition
        int newTotalMembers = currentMemberCount + membersToAdd;

        // Check if tier has a maximum (null = unlimited for highest tier)
        Integer tierMaxMembers = tier.getMaxMembers();
        if (tierMaxMembers == null) {
            // Unlimited tier (Enterprise) - always allow
            log.debug("Church {} is on unlimited tier {}. Allowing {} members (current: {}, new total: {})",
                    churchId, tier.getTierName(), membersToAdd, currentMemberCount, newTotalMembers);

            return new TierCheckResult(
                    true,
                    currentMemberCount,
                    null,
                    membersToAdd,
                    newTotalMembers,
                    0.0,
                    "Unlimited tier - no member limit"
            );
        }

        // Calculate buffer (1% of tier max, minimum 5 members)
        int buffer = Math.max(5, (int) (tierMaxMembers * BUFFER_PERCENTAGE));
        int effectiveLimit = tierMaxMembers + buffer;

        // Check if new total would exceed limit (with buffer)
        boolean allowed = newTotalMembers <= effectiveLimit;

        // Calculate percentage of tier limit
        double percentageUsed = (newTotalMembers * 100.0) / tierMaxMembers;

        if (!allowed) {
            log.warn("Tier limit exceeded for church {}: current={}, adding={}, new total={}, tier max={}, effective limit={}",
                    churchId, currentMemberCount, membersToAdd, newTotalMembers, tierMaxMembers, effectiveLimit);
        } else if (percentageUsed > 95.0) {
            log.warn("Church {} approaching tier limit: {:.1f}% of tier max ({}/{})",
                    churchId, percentageUsed, newTotalMembers, tierMaxMembers);
        }

        String message = allowed
                ? String.format("Within tier limit (%d/%d members, %.1f%%)", newTotalMembers, tierMaxMembers, percentageUsed)
                : String.format("Would exceed tier limit: %d + %d = %d members (tier max: %d, limit with buffer: %d). " +
                                "Please upgrade to %s tier or remove %d members before importing.",
                        currentMemberCount, membersToAdd, newTotalMembers, tierMaxMembers, effectiveLimit,
                        getNextTierName(tier), newTotalMembers - effectiveLimit);

        return new TierCheckResult(
                allowed,
                currentMemberCount,
                tierMaxMembers,
                membersToAdd,
                newTotalMembers,
                percentageUsed,
                message
        );
    }

    /**
     * Enforce tier limit BEFORE member creation.
     * Throws exception if limit would be exceeded.
     *
     * <p><strong>USE THIS METHOD</strong> in all member creation endpoints.
     *
     * @param churchId Church ID
     * @param membersToAdd Number of members to add
     * @throws TierLimitExceededException if tier limit would be exceeded
     */
    public void enforceTierLimit(Long churchId, int membersToAdd) {
        TierCheckResult result = canAddMembers(churchId, membersToAdd);
        if (!result.isAllowed()) {
            throw new TierLimitExceededException(
                    result.getMessage(),
                    result.getCurrentMemberCount(),
                    result.getTierMaxMembers(),
                    result.getMembersToAdd(),
                    result.getNewTotalMembers(),
                    result.getPercentageUsed()
            );
        }
    }

    /**
     * Check if church is approaching tier limit (>80%).
     *
     * @param churchId Church ID
     * @return true if at or above 80% of tier limit
     */
    @Transactional(readOnly = true)
    public boolean isApproachingTierLimit(Long churchId) {
        TierCheckResult result = canAddMembers(churchId, 0);
        return result.getPercentageUsed() >= 80.0;
    }

    /**
     * Get warning message if church is approaching tier limit.
     *
     * @param churchId Church ID
     * @return Warning message, or null if not approaching limit
     */
    @Transactional(readOnly = true)
    public String getTierLimitWarning(Long churchId) {
        TierCheckResult result = canAddMembers(churchId, 0);

        if (result.getPercentageUsed() >= 95.0) {
            return String.format(
                    "CRITICAL: You've used %.0f%% of your tier's member limit (%d/%d members). " +
                    "Upgrade to avoid service interruption.",
                    result.getPercentageUsed(),
                    result.getCurrentMemberCount(),
                    result.getTierMaxMembers()
            );
        } else if (result.getPercentageUsed() >= 80.0) {
            return String.format(
                    "Warning: You've used %.0f%% of your tier's member limit (%d/%d members). " +
                    "Consider upgrading soon.",
                    result.getPercentageUsed(),
                    result.getCurrentMemberCount(),
                    result.getTierMaxMembers()
            );
        }

        return null;
    }

    /**
     * Calculate how many members church can still add without exceeding tier.
     *
     * @param churchId Church ID
     * @return Number of members that can be added (null if unlimited)
     */
    @Transactional(readOnly = true)
    public Integer getRemainingMemberCapacity(Long churchId) {
        TierCheckResult result = canAddMembers(churchId, 0);

        if (result.getTierMaxMembers() == null) {
            return null; // Unlimited
        }

        int remaining = result.getTierMaxMembers() - result.getCurrentMemberCount();
        return Math.max(0, remaining);
    }

    /**
     * Get the next higher tier name for upgrade messaging.
     *
     * @param currentTier Current pricing tier
     * @return Next tier display name
     */
    private String getNextTierName(CongregationPricingTier currentTier) {
        // Simple logic based on tier naming convention
        String tierName = currentTier.getTierName();

        return switch (tierName) {
            case "TIER_1" -> "Growing Church (201-500)";
            case "TIER_2" -> "Medium Church (501-1000)";
            case "TIER_3" -> "Large Church (1001-2000)";
            case "TIER_4" -> "Enterprise (2001+)";
            default -> "next tier";
        };
    }

    /**
     * Result of tier limit check with detailed metrics.
     */
    @Getter
    public static class TierCheckResult {
        private final boolean allowed;
        private final int currentMemberCount;
        private final Integer tierMaxMembers; // null = unlimited
        private final int membersToAdd;
        private final int newTotalMembers;
        private final double percentageUsed;
        private final String message;

        public TierCheckResult(
                boolean allowed,
                int currentMemberCount,
                Integer tierMaxMembers,
                int membersToAdd,
                int newTotalMembers,
                double percentageUsed,
                String message
        ) {
            this.allowed = allowed;
            this.currentMemberCount = currentMemberCount;
            this.tierMaxMembers = tierMaxMembers;
            this.membersToAdd = membersToAdd;
            this.newTotalMembers = newTotalMembers;
            this.percentageUsed = percentageUsed;
            this.message = message;
        }

        /**
         * Check if approaching limit (>80%).
         */
        public boolean isApproachingLimit() {
            return percentageUsed >= 80.0;
        }

        /**
         * Check if critically close to limit (>95%).
         */
        public boolean isCriticallyClose() {
            return percentageUsed >= 95.0;
        }
    }
}
