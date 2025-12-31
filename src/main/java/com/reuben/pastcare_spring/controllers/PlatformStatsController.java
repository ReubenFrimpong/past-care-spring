package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.dtos.ChurchSummaryResponse;
import com.reuben.pastcare_spring.dtos.PlatformStatsResponse;
import com.reuben.pastcare_spring.dtos.PlatformStorageStatsResponse;
import com.reuben.pastcare_spring.dtos.ChurchStorageSummaryResponse;
import com.reuben.pastcare_spring.dtos.PlatformBillingStatsResponse;
import com.reuben.pastcare_spring.dtos.RecentPaymentResponse;
import com.reuben.pastcare_spring.dtos.OverdueSubscriptionResponse;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.services.PlatformStatsService;
import com.reuben.pastcare_spring.services.PlatformStorageService;
import com.reuben.pastcare_spring.services.PlatformBillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Platform admin controller for SUPERADMIN users only.
 * Provides multi-tenant overview, church management, and system-wide statistics.
 */
@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@Slf4j
public class PlatformStatsController {

    private final PlatformStatsService platformStatsService;
    private final PlatformStorageService platformStorageService;
    private final PlatformBillingService platformBillingService;

    /**
     * Get platform-wide statistics (SUPERADMIN only).
     * Returns aggregated data across all churches.
     */
    @GetMapping("/stats")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<PlatformStatsResponse> getPlatformStats() {
        log.info("Fetching platform-wide statistics");
        PlatformStatsResponse stats = platformStatsService.getPlatformStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all church summaries with pagination (SUPERADMIN only).
     */
    @GetMapping("/churches")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<Page<ChurchSummaryResponse>> getChurchSummaries(Pageable pageable) {
        log.info("Fetching church summaries with pagination");
        Page<ChurchSummaryResponse> churches = platformStatsService.getChurchSummaries(pageable);
        return ResponseEntity.ok(churches);
    }

    /**
     * Get all church summaries without pagination (SUPERADMIN only).
     */
    @GetMapping("/churches/all")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<List<ChurchSummaryResponse>> getAllChurchSummaries() {
        log.info("Fetching all church summaries");
        List<ChurchSummaryResponse> churches = platformStatsService.getAllChurchSummaries();
        return ResponseEntity.ok(churches);
    }

    /**
     * Get church summary by ID (SUPERADMIN only).
     */
    @GetMapping("/churches/{id}")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<ChurchSummaryResponse> getChurchSummary(@PathVariable Long id) {
        log.info("Fetching church summary for ID: {}", id);
        ChurchSummaryResponse church = platformStatsService.getChurchSummary(id);
        return ResponseEntity.ok(church);
    }

    /**
     * Activate a church (SUPERADMIN only).
     */
    @PostMapping("/churches/{id}/activate")
    @RequirePermission(Permission.PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<Void> activateChurch(@PathVariable Long id) {
        log.info("Activating church with ID: {}", id);
        platformStatsService.activateChurch(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Deactivate a church (SUPERADMIN only).
     */
    @PostMapping("/churches/{id}/deactivate")
    @RequirePermission(Permission.PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<Void> deactivateChurch(@PathVariable Long id) {
        log.info("Deactivating church with ID: {}", id);
        platformStatsService.deactivateChurch(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Get platform-wide storage statistics (SUPERADMIN only).
     * Returns aggregated storage data across all churches.
     */
    @GetMapping("/storage/stats")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<PlatformStorageStatsResponse> getPlatformStorageStats() {
        log.info("Fetching platform-wide storage statistics");
        PlatformStorageStatsResponse stats = platformStorageService.getPlatformStorageStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get top storage consumers (SUPERADMIN only).
     * Returns churches with highest storage usage.
     */
    @GetMapping("/storage/top-consumers")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<List<ChurchStorageSummaryResponse>> getTopStorageConsumers(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching top {} storage consumers", limit);
        List<ChurchStorageSummaryResponse> consumers = platformStorageService.getTopStorageConsumers(limit);
        return ResponseEntity.ok(consumers);
    }

    /**
     * Get all church storage summaries (SUPERADMIN only).
     */
    @GetMapping("/storage/all-churches")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<List<ChurchStorageSummaryResponse>> getAllChurchStorageSummaries() {
        log.info("Fetching all church storage summaries");
        List<ChurchStorageSummaryResponse> summaries = platformStorageService.getAllChurchStorageSummaries();
        return ResponseEntity.ok(summaries);
    }

    /**
     * Get platform-wide billing statistics (SUPERADMIN only).
     * Returns revenue metrics, subscription distribution, and churn rate.
     */
    @GetMapping("/billing/stats")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<PlatformBillingStatsResponse> getPlatformBillingStats() {
        log.info("Fetching platform-wide billing statistics");
        PlatformBillingStatsResponse stats = platformBillingService.getPlatformBillingStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get recent payments across all churches (SUPERADMIN only).
     */
    @GetMapping("/billing/recent-payments")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<List<RecentPaymentResponse>> getRecentPayments(
            @RequestParam(defaultValue = "20") int limit) {
        log.info("Fetching recent {} payments", limit);
        List<RecentPaymentResponse> payments = platformBillingService.getRecentPayments(limit);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get overdue subscriptions (SUPERADMIN only).
     * Returns churches with past due or suspended subscriptions.
     */
    @GetMapping("/billing/overdue-subscriptions")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<List<OverdueSubscriptionResponse>> getOverdueSubscriptions() {
        log.info("Fetching overdue subscriptions");
        List<OverdueSubscriptionResponse> overdueList = platformBillingService.getOverdueSubscriptions();
        return ResponseEntity.ok(overdueList);
    }
}
