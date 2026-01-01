package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing migration from storage-based to congregation-based pricing.
 *
 * <p>Handles:
 * <ul>
 *   <li>Individual church migrations</li>
 *   <li>Bulk migrations for all churches</li>
 *   <li>Rollback to storage-based pricing (emergency only)</li>
 *   <li>Migration status reporting</li>
 *   <li>Audit trail tracking</li>
 * </ul>
 *
 * @since 2026-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PricingMigrationService {

    private final ChurchRepository churchRepository;
    private final ChurchSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CongregationPricingTierRepository tierRepository;
    private final SubscriptionBillingIntervalRepository billingIntervalRepository;
    private final PricingModelMigrationRepository migrationRepository;
    private final MemberRepository memberRepository;
    private final CongregationPricingService pricingService;

    /**
     * Migrate a single church from storage-based to congregation-based pricing.
     *
     * <p>Steps:
     * <ol>
     *   <li>Get current member count</li>
     *   <li>Determine appropriate pricing tier</li>
     *   <li>Assign tier to church subscription</li>
     *   <li>Create migration audit record</li>
     * </ol>
     *
     * @param churchId Church to migrate
     * @param performedBy SUPERADMIN user ID (null for automated migration)
     * @return Migration result
     * @throws IllegalArgumentException if church not found or already migrated
     */
    @Transactional
    public MigrationResult migrateChurch(Long churchId, Long performedBy) {
        log.info("Starting migration for church {} (performed by user: {})", churchId, performedBy);

        // Check if already migrated
        if (migrationRepository.existsByChurchId(churchId)) {
            throw new IllegalStateException(
                    "Church " + churchId + " has already been migrated. Use rollback first if needed.");
        }

        // Get church and subscription
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Church not found: " + churchId));

        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription found for church: " + churchId));

        // Get old plan details (for audit trail)
        SubscriptionPlan oldPlan = subscription.getPlan();
        Long oldStorageLimitMb = oldPlan != null ? oldPlan.getStorageLimitMb() : null;
        BigDecimal oldMonthlyPrice = oldPlan != null ? oldPlan.getPrice() : BigDecimal.ZERO;

        // Get current member count
        int memberCount = (int) memberRepository.countByChurchId(churchId);

        // Determine appropriate pricing tier
        CongregationPricingTier tier = pricingService.getPricingTierForMemberCount(memberCount);

        // Get default billing interval (MONTHLY)
        SubscriptionBillingInterval billingInterval = billingIntervalRepository
                .findByIntervalName("MONTHLY")
                .orElseThrow(() -> new IllegalStateException("MONTHLY billing interval not found"));

        // Calculate new price
        BigDecimal newMonthlyPrice = tier.getMonthlyPriceUsd();

        // Update subscription with new pricing
        subscription.setPricingTier(tier);
        subscription.setBillingInterval(billingInterval);
        subscription.setCurrentMemberCount(memberCount);
        subscription.setMemberCountLastChecked(LocalDateTime.now());
        subscription.setSubscriptionAmount(newMonthlyPrice);

        // Update church cache
        church.setCachedMemberCount(memberCount);
        church.setMemberCountLastUpdated(LocalDateTime.now());
        church.setEligiblePricingTierId(tier.getId());

        // Save updates
        churchRepository.save(church);
        subscriptionRepository.save(subscription);

        // Create migration audit record
        PricingModelMigration migration = PricingModelMigration.builder()
                .churchId(churchId)
                .oldPlanId(oldPlan != null ? oldPlan.getId() : null)
                .oldStorageLimitMb(oldStorageLimitMb)
                .oldMonthlyPrice(oldMonthlyPrice)
                .newPricingTierId(tier.getId())
                .newMemberCount(memberCount)
                .newMonthlyPrice(newMonthlyPrice)
                .migratedBy(performedBy)
                .migrationStatus(PricingModelMigration.MigrationStatus.COMPLETED)
                .migrationNotes(String.format(
                        "Migrated from %s (storage-based) to %s (congregation-based). " +
                        "Member count: %d. Old price: $%.2f/month, New price: $%.2f/month",
                        oldPlan != null ? oldPlan.getName() : "UNKNOWN",
                        tier.getTierName(),
                        memberCount,
                        oldMonthlyPrice,
                        newMonthlyPrice
                ))
                .build();

        migrationRepository.save(migration);

        MigrationResult result = new MigrationResult(
                churchId,
                church.getName(),
                true,
                oldPlan != null ? oldPlan.getName() : "UNKNOWN",
                tier.getTierName(),
                memberCount,
                oldMonthlyPrice,
                newMonthlyPrice,
                null
        );

        log.info("Migration completed for church {}: {} → {}. Members: {}, Price: ${} → ${}",
                churchId, result.oldPlanName(), result.newTierName(),
                memberCount, oldMonthlyPrice, newMonthlyPrice);

        return result;
    }

    /**
     * Migrate all churches from storage-based to congregation-based pricing.
     *
     * <p>This is a bulk operation that should be run once during platform migration.
     *
     * @param performedBy SUPERADMIN user ID
     * @return Summary of migration results
     */
    @Transactional
    public MigrationSummary bulkMigrateAllChurches(Long performedBy) {
        log.info("Starting bulk migration of all churches (performed by user: {})", performedBy);

        long startTime = System.currentTimeMillis();
        List<Church> allChurches = churchRepository.findAll();

        int totalChurches = allChurches.size();
        int successCount = 0;
        int failureCount = 0;
        List<MigrationResult> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Church church : allChurches) {
            try {
                // Skip if already migrated
                if (migrationRepository.existsByChurchId(church.getId())) {
                    log.debug("Church {} already migrated, skipping", church.getId());
                    continue;
                }

                MigrationResult result = migrateChurch(church.getId(), performedBy);
                results.add(result);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                String error = String.format("Church %d (%s): %s",
                        church.getId(), church.getName(), e.getMessage());
                errors.add(error);
                log.error("Failed to migrate church {}: {}", church.getId(), e.getMessage(), e);
            }
        }

        long durationMs = System.currentTimeMillis() - startTime;

        MigrationSummary summary = new MigrationSummary(
                totalChurches,
                successCount,
                failureCount,
                results,
                errors,
                durationMs
        );

        log.info("Bulk migration completed: {}", summary);

        return summary;
    }

    /**
     * Rollback a church migration (emergency use only).
     *
     * <p><strong>WARNING:</strong> This restores the old storage-based pricing.
     * Use only if congregation-based pricing is causing issues.
     *
     * @param churchId Church to rollback
     * @param performedBy SUPERADMIN user ID
     * @param reason Reason for rollback
     * @throws IllegalArgumentException if church not found or not migrated
     */
    @Transactional
    public void rollbackMigration(Long churchId, Long performedBy, String reason) {
        log.warn("Rolling back migration for church {} (performed by user: {}, reason: {})",
                churchId, performedBy, reason);

        // Get latest migration record
        PricingModelMigration migration = migrationRepository.findLatestByChurchId(churchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No migration found for church: " + churchId));

        if (!migration.canRollback()) {
            throw new IllegalStateException(
                    "Migration cannot be rolled back. Status: " + migration.getMigrationStatus());
        }

        // Get church and subscription
        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription found"));

        // Restore old plan
        if (migration.getOldPlanId() != null) {
            SubscriptionPlan oldPlan = subscriptionPlanRepository.findById(migration.getOldPlanId())
                    .orElseThrow(() -> new IllegalStateException("Old plan not found"));

            subscription.setPlan(oldPlan);
            subscription.setPricingTier(null);
            subscription.setBillingInterval(null);
            subscription.setSubscriptionAmount(migration.getOldMonthlyPrice());

            subscriptionRepository.save(subscription);
        }

        // Update migration record
        migration.setMigrationStatus(PricingModelMigration.MigrationStatus.ROLLED_BACK);
        migration.setMigrationNotes(migration.getMigrationNotes() + "\n\n--- ROLLBACK ---\n" +
                "Rolled back at: " + LocalDateTime.now() + "\n" +
                "Rolled back by user ID: " + performedBy + "\n" +
                "Reason: " + reason);

        migrationRepository.save(migration);

        log.info("Migration rollback completed for church {}", churchId);
    }

    /**
     * Get migration status for the entire platform.
     *
     * @return Migration status summary
     */
    @Transactional(readOnly = true)
    public MigrationStatusReport getMigrationStatus() {
        List<PricingModelMigration> allMigrations = migrationRepository.findAll();

        long totalMigrations = allMigrations.size();
        long completed = allMigrations.stream()
                .filter(m -> m.getMigrationStatus() == PricingModelMigration.MigrationStatus.COMPLETED)
                .count();
        long rolledBack = allMigrations.stream()
                .filter(m -> m.getMigrationStatus() == PricingModelMigration.MigrationStatus.ROLLED_BACK)
                .count();
        long failed = allMigrations.stream()
                .filter(m -> m.getMigrationStatus() == PricingModelMigration.MigrationStatus.FAILED)
                .count();

        // Calculate average price change
        BigDecimal avgPriceChange = allMigrations.stream()
                .filter(m -> m.getMigrationStatus() == PricingModelMigration.MigrationStatus.COMPLETED)
                .map(PricingModelMigration::getPriceChange)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, completed)), 2, java.math.RoundingMode.HALF_UP);

        // Tier distribution
        var tierDistribution = allMigrations.stream()
                .filter(m -> m.getMigrationStatus() == PricingModelMigration.MigrationStatus.COMPLETED)
                .collect(java.util.stream.Collectors.groupingBy(
                        PricingModelMigration::getNewPricingTierId,
                        java.util.stream.Collectors.counting()
                ));

        return new MigrationStatusReport(
                totalMigrations,
                completed,
                rolledBack,
                failed,
                avgPriceChange,
                tierDistribution
        );
    }

    /**
     * Check if a church can be migrated.
     *
     * @param churchId Church ID
     * @return true if migration is possible
     */
    public boolean canMigrate(Long churchId) {
        // Check if already migrated
        if (migrationRepository.existsByChurchId(churchId)) {
            return false;
        }

        // Check if church and subscription exist
        if (!churchRepository.existsById(churchId)) {
            return false;
        }

        return subscriptionRepository.findByChurchId(churchId).isPresent();
    }

    /**
     * Result of a single church migration.
     */
    public record MigrationResult(
            Long churchId,
            String churchName,
            boolean success,
            String oldPlanName,
            String newTierName,
            int memberCount,
            BigDecimal oldMonthlyPrice,
            BigDecimal newMonthlyPrice,
            String errorMessage
    ) {
        public BigDecimal getPriceChange() {
            return newMonthlyPrice.subtract(oldMonthlyPrice);
        }

        public boolean isPriceIncrease() {
            return getPriceChange().compareTo(BigDecimal.ZERO) > 0;
        }
    }

    /**
     * Summary of bulk migration operation.
     */
    public record MigrationSummary(
            int totalChurches,
            int successCount,
            int failureCount,
            List<MigrationResult> results,
            List<String> errors,
            long durationMs
    ) {
        public double getSuccessRate() {
            return totalChurches == 0 ? 0 : (successCount * 100.0) / totalChurches;
        }

        @Override
        public String toString() {
            return String.format(
                    "%d churches migrated (%d successful, %d failed, %.1f%% success rate). Took %dms",
                    totalChurches, successCount, failureCount, getSuccessRate(), durationMs
            );
        }
    }

    /**
     * Platform-wide migration status report.
     */
    public record MigrationStatusReport(
            long totalMigrations,
            long completed,
            long rolledBack,
            long failed,
            BigDecimal avgPriceChange,
            java.util.Map<Long, Long> tierDistribution
    ) {
        public double getCompletionRate() {
            return totalMigrations == 0 ? 0 : (completed * 100.0) / totalMigrations;
        }
    }
}
