package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.dtos.ChurchSummaryResponse;
import com.reuben.pastcare_spring.dtos.PlatformStatsResponse;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.services.PlatformStatsService;
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
}
