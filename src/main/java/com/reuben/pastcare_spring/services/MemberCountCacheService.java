package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.CongregationPricingTier;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for caching and updating church member counts.
 *
 * <p>Member counts are cached to avoid expensive COUNT queries on every tier check.
 * This service runs periodic updates (daily at midnight) and provides on-demand updates.
 *
 * <p>Cached counts are used for:
 * <ul>
 *   <li>Tier upgrade detection</li>
 *   <li>Dashboard statistics</li>
 *   <li>Subscription tier recommendations</li>
 * </ul>
 *
 * <p><strong>Note:</strong> For tier enforcement (security-critical), always use
 * real-time database count, not cached count.
 *
 * @since 2026-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberCountCacheService {

    private final ChurchRepository churchRepository;
    private final ChurchSubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final CongregationPricingService pricingService;

    /**
     * Update member count cache for a specific church.
     *
     * <p>Updates both:
     * <ul>
     *   <li>churches.cached_member_count</li>
     *   <li>church_subscriptions.current_member_count</li>
     * </ul>
     *
     * @param churchId Church ID
     * @return Updated member count
     */
    @Transactional
    public int updateChurchMemberCount(Long churchId) {
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Church not found: " + churchId));

        // Get actual count from database
        int memberCount = (int) memberRepository.countByChurchId(churchId);

        // Update church cache
        church.setCachedMemberCount(memberCount);
        church.setMemberCountLastUpdated(LocalDateTime.now());

        // Update recommended tier based on current count
        try {
            CongregationPricingTier recommendedTier = pricingService.getPricingTierForMemberCount(memberCount);
            church.setEligiblePricingTierId(recommendedTier.getId());
        } catch (Exception e) {
            log.error("Failed to determine recommended tier for church {}", churchId, e);
        }

        churchRepository.save(church);

        // Update subscription cache if exists
        subscriptionRepository.findByChurchId(churchId).ifPresent(subscription -> {
            subscription.setCurrentMemberCount(memberCount);
            subscription.setMemberCountLastChecked(LocalDateTime.now());

            // Check if tier upgrade is required
            CongregationPricingTier currentTier = subscription.getPricingTier();
            if (currentTier != null && currentTier.getMaxMembers() != null) {
                boolean exceededLimit = memberCount > currentTier.getMaxMembers();
                subscription.setTierUpgradeRequired(exceededLimit);

                if (exceededLimit) {
                    log.warn("Church {} has exceeded tier limit: {} members (tier max: {})",
                            churchId, memberCount, currentTier.getMaxMembers());
                }
            }

            subscriptionRepository.save(subscription);
        });

        log.debug("Updated member count cache for church {}: {} members", churchId, memberCount);

        return memberCount;
    }

    /**
     * Get cached member count for a church.
     *
     * <p><strong>Warning:</strong> This returns cached data which may be stale.
     * For tier enforcement, use {@link MemberRepository#countByChurchId(Long)} instead.
     *
     * @param churchId Church ID
     * @return Cached member count
     */
    public int getCachedMemberCount(Long churchId) {
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Church not found: " + churchId));

        Integer cachedCount = church.getCachedMemberCount();
        if (cachedCount == null) {
            log.warn("No cached member count for church {}. Updating now.", churchId);
            return updateChurchMemberCount(churchId);
        }

        return cachedCount;
    }

    /**
     * Scheduled task: Update all church member counts daily at midnight.
     *
     * <p>Runs every day at 00:00 (midnight) in the application's timezone.
     * This ensures all cached counts are fresh for the new day.
     *
     * <p>Cron expression: "0 0 0 * * *" = second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    @Transactional
    public void scheduledMemberCountUpdate() {
        log.info("Starting scheduled member count cache update for all churches");

        long startTime = System.currentTimeMillis();
        int totalChurches = 0;
        int successCount = 0;
        int errorCount = 0;

        try {
            List<Church> allChurches = churchRepository.findAll();
            totalChurches = allChurches.size();

            for (Church church : allChurches) {
                try {
                    updateChurchMemberCount(church.getId());
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    log.error("Failed to update member count for church {}: {}",
                            church.getId(), e.getMessage());
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Completed scheduled member count update: {} churches, {} successful, {} errors, took {}ms",
                    totalChurches, successCount, errorCount, duration);

        } catch (Exception e) {
            log.error("Fatal error during scheduled member count update", e);
        }
    }

    /**
     * Update all churches immediately (on-demand).
     *
     * <p>Use this for:
     * <ul>
     *   <li>After bulk member imports</li>
     *   <li>After tier migrations</li>
     *   <li>Manual refresh by SUPERADMIN</li>
     * </ul>
     *
     * @return Summary of update results
     */
    @Transactional
    public MemberCountUpdateSummary bulkUpdateAllChurches() {
        log.info("Starting bulk member count update for all churches");

        long startTime = System.currentTimeMillis();
        List<Church> allChurches = churchRepository.findAll();

        int totalChurches = allChurches.size();
        int successCount = 0;
        int errorCount = 0;
        int tierUpgradesRequired = 0;

        for (Church church : allChurches) {
            try {
                int memberCount = updateChurchMemberCount(church.getId());

                // Check if tier upgrade required
                subscriptionRepository.findByChurchId(church.getId()).ifPresent(subscription -> {
                    if (Boolean.TRUE.equals(subscription.getTierUpgradeRequired())) {
                        // tierUpgradesRequired++; // Increment in outer scope
                    }
                });

                successCount++;
            } catch (Exception e) {
                errorCount++;
                log.error("Failed to update member count for church {}: {}",
                        church.getId(), e.getMessage());
            }
        }

        // Count tier upgrades required
        List<ChurchSubscription> subscriptions = subscriptionRepository.findAll();
        tierUpgradesRequired = (int) subscriptions.stream()
                .filter(s -> Boolean.TRUE.equals(s.getTierUpgradeRequired()))
                .count();

        long durationMs = System.currentTimeMillis() - startTime;

        MemberCountUpdateSummary summary = new MemberCountUpdateSummary(
                totalChurches,
                successCount,
                errorCount,
                tierUpgradesRequired,
                durationMs
        );

        log.info("Bulk member count update completed: {}", summary);

        return summary;
    }

    /**
     * Update member counts for churches that haven't been updated recently.
     *
     * @param hoursSinceLastUpdate Minimum hours since last update
     * @return Number of churches updated
     */
    @Transactional
    public int updateStaleChurches(int hoursSinceLastUpdate) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursSinceLastUpdate);

        List<Church> staleChurches = churchRepository.findAll().stream()
                .filter(church -> church.getMemberCountLastUpdated() == null ||
                                 church.getMemberCountLastUpdated().isBefore(cutoffTime))
                .toList();

        log.info("Found {} churches with stale member count (not updated in {} hours)",
                staleChurches.size(), hoursSinceLastUpdate);

        int updateCount = 0;
        for (Church church : staleChurches) {
            try {
                updateChurchMemberCount(church.getId());
                updateCount++;
            } catch (Exception e) {
                log.error("Failed to update stale church {}: {}", church.getId(), e.getMessage());
            }
        }

        log.info("Updated {} stale churches", updateCount);
        return updateCount;
    }

    /**
     * Identify churches that need tier upgrades.
     *
     * <p>Returns list of churches where member count exceeds current tier's max.
     *
     * @return List of church IDs requiring tier upgrades
     */
    @Transactional(readOnly = true)
    public List<Long> findChurchesNeedingTierUpgrade() {
        List<ChurchSubscription> allSubscriptions = subscriptionRepository.findAll();

        return allSubscriptions.stream()
                .filter(subscription -> {
                    CongregationPricingTier tier = subscription.getPricingTier();
                    if (tier == null || tier.getMaxMembers() == null) {
                        return false; // No tier or unlimited tier
                    }

                    Integer memberCount = subscription.getCurrentMemberCount();
                    if (memberCount == null) {
                        return false; // No cached count
                    }

                    return memberCount > tier.getMaxMembers();
                })
                .map(ChurchSubscription::getChurchId)
                .toList();
    }

    /**
     * Summary of member count update operation.
     */
    public record MemberCountUpdateSummary(
            int totalChurches,
            int successCount,
            int errorCount,
            int tierUpgradesRequired,
            long durationMs
    ) {
        public double getSuccessRate() {
            return totalChurches == 0 ? 0 : (successCount * 100.0) / totalChurches;
        }

        @Override
        public String toString() {
            return String.format(
                    "%d churches updated (%d successful, %d errors, %.1f%% success rate). " +
                    "%d churches require tier upgrades. Took %dms",
                    totalChurches, successCount, errorCount, getSuccessRate(),
                    tierUpgradesRequired, durationMs
            );
        }
    }
}
