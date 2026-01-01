package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.services.PricingMigrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for pricing model migration management.
 *
 * <p><b>ALL ENDPOINTS REQUIRE SUPERADMIN AUTHORITY</b>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/platform/pricing-migration/migrate-church - Migrate single church</li>
 *   <li>POST /api/platform/pricing-migration/bulk-migrate - Migrate all churches</li>
 *   <li>POST /api/platform/pricing-migration/rollback - Rollback church migration</li>
 *   <li>GET /api/platform/pricing-migration/status - Get migration status</li>
 *   <li>GET /api/platform/pricing-migration/can-migrate/{churchId} - Check if church can be migrated</li>
 * </ul>
 *
 * @since 2026-01-01
 */
@RestController
@RequestMapping("/api/platform/pricing-migration")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('SUPERADMIN')")
public class PricingMigrationController {

    private final PricingMigrationService migrationService;

    /**
     * Migrate a single church from storage-based to congregation-based pricing.
     *
     * <p><b>SUPERADMIN only</b>
     *
     * <p>Steps performed:
     * <ol>
     *   <li>Get current member count</li>
     *   <li>Determine appropriate pricing tier</li>
     *   <li>Assign tier to church subscription</li>
     *   <li>Create migration audit record</li>
     * </ol>
     *
     * @param request Migration request with church ID and admin user ID
     * @return Migration result
     */
    @PostMapping("/migrate-church")
    public ResponseEntity<PricingMigrationService.MigrationResult> migrateChurch(
            @Valid @RequestBody ChurchMigrationRequest request) {

        log.info("POST /api/platform/pricing-migration/migrate-church - churchId={}, performedBy={}",
                request.churchId(), request.performedBy());

        PricingMigrationService.MigrationResult result = migrationService.migrateChurch(
                request.churchId(),
                request.performedBy()
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Migrate ALL churches from storage-based to congregation-based pricing.
     *
     * <p><b>SUPERADMIN only</b>
     *
     * <p><strong>WARNING:</strong> This is a bulk operation that migrates every church
     * in the platform. Use with caution in production.
     *
     * <p>Recommended approach:
     * <ol>
     *   <li>Test migration on staging first</li>
     *   <li>Schedule during low-traffic period</li>
     *   <li>Notify all churches in advance</li>
     *   <li>Monitor for errors during migration</li>
     *   <li>Have rollback plan ready</li>
     * </ol>
     *
     * @param request Bulk migration request with admin user ID
     * @return Migration summary with success/failure counts
     */
    @PostMapping("/bulk-migrate")
    public ResponseEntity<PricingMigrationService.MigrationSummary> bulkMigrateAllChurches(
            @Valid @RequestBody BulkMigrationRequest request) {

        log.warn("POST /api/platform/pricing-migration/bulk-migrate - STARTING BULK MIGRATION (performedBy={})",
                request.performedBy());

        long startTime = System.currentTimeMillis();

        PricingMigrationService.MigrationSummary summary = migrationService.bulkMigrateAllChurches(
                request.performedBy()
        );

        long durationSeconds = (System.currentTimeMillis() - startTime) / 1000;

        log.warn("POST /api/platform/pricing-migration/bulk-migrate - COMPLETED. " +
                "Success: {}, Failed: {}, Duration: {}s",
                summary.successCount(), summary.failureCount(), durationSeconds);

        return ResponseEntity.ok(summary);
    }

    /**
     * Rollback a church migration (emergency use only).
     *
     * <p><b>SUPERADMIN only</b>
     *
     * <p><strong>WARNING:</strong> This restores the old storage-based pricing.
     * Use only if congregation-based pricing is causing issues for this church.
     *
     * @param request Rollback request
     * @return Success message
     */
    @PostMapping("/rollback")
    public ResponseEntity<Map<String, Object>> rollbackMigration(
            @Valid @RequestBody RollbackRequest request) {

        log.warn("POST /api/platform/pricing-migration/rollback - churchId={}, reason={}",
                request.churchId(), request.reason());

        migrationService.rollbackMigration(
                request.churchId(),
                request.performedBy(),
                request.reason()
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Migration rolled back successfully",
                "churchId", request.churchId(),
                "reason", request.reason()
        ));
    }

    /**
     * Get platform-wide migration status.
     *
     * <p><b>SUPERADMIN only</b>
     *
     * <p>Returns:
     * <ul>
     *   <li>Total migrations performed</li>
     *   <li>Completed, rolled back, failed counts</li>
     *   <li>Average price change</li>
     *   <li>Tier distribution</li>
     * </ul>
     *
     * @return Migration status report
     */
    @GetMapping("/status")
    public ResponseEntity<PricingMigrationService.MigrationStatusReport> getMigrationStatus() {
        log.debug("GET /api/platform/pricing-migration/status - Fetching migration status");

        PricingMigrationService.MigrationStatusReport report = migrationService.getMigrationStatus();

        return ResponseEntity.ok(report);
    }

    /**
     * Check if a church can be migrated.
     *
     * <p><b>SUPERADMIN only</b>
     *
     * <p>Returns false if:
     * <ul>
     *   <li>Church already migrated</li>
     *   <li>Church not found</li>
     *   <li>Church has no subscription</li>
     * </ul>
     *
     * @param churchId Church ID to check
     * @return Can migrate status
     */
    @GetMapping("/can-migrate/{churchId}")
    public ResponseEntity<Map<String, Object>> canMigrate(@PathVariable Long churchId) {
        log.debug("GET /api/platform/pricing-migration/can-migrate/{} - Checking eligibility", churchId);

        boolean canMigrate = migrationService.canMigrate(churchId);

        String reason = canMigrate
                ? "Church is eligible for migration"
                : "Church is not eligible (already migrated, not found, or no subscription)";

        return ResponseEntity.ok(Map.of(
                "churchId", churchId,
                "canMigrate", canMigrate,
                "reason", reason
        ));
    }

    // ==================== DTOs ====================

    /**
     * Request to migrate a single church.
     */
    public record ChurchMigrationRequest(
            @NotNull
            Long churchId,

            @NotNull
            Long performedBy // SUPERADMIN user ID
    ) {}

    /**
     * Request to migrate all churches.
     */
    public record BulkMigrationRequest(
            @NotNull
            Long performedBy // SUPERADMIN user ID
    ) {}

    /**
     * Request to rollback a migration.
     */
    public record RollbackRequest(
            @NotNull
            Long churchId,

            @NotNull
            Long performedBy, // SUPERADMIN user ID

            @NotBlank
            String reason // Reason for rollback (for audit trail)
    ) {}
}
