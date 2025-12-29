package com.reuben.pastcare_spring.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.dtos.StorageUsageResponse;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.models.StorageUsage;
import com.reuben.pastcare_spring.services.StorageCalculationService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for storage usage tracking and billing.
 * Allows churches to view their storage consumption and history.
 */
@RestController
@RequestMapping("/api/storage-usage")
@RequiredArgsConstructor
@Tag(name = "Storage Usage", description = "Storage usage tracking for billing")
public class StorageUsageController {

    private final StorageCalculationService storageCalculationService;
    private final RequestContextUtil requestContextUtil;
    private final ObjectMapper objectMapper;

    private static final double DEFAULT_STORAGE_LIMIT_MB = 2048.0; // 2 GB base plan

    /**
     * Get current storage usage for the authenticated church.
     */
    @GetMapping("/current")
    @RequirePermission({Permission.SUBSCRIPTION_VIEW, Permission.CHURCH_SETTINGS_VIEW})
    @Operation(summary = "Get current storage usage", description = "Returns the latest storage usage for the current church")
    public ResponseEntity<StorageUsageResponse> getCurrentStorageUsage(HttpServletRequest request) {
        Long churchId = requestContextUtil.extractChurchId(request);
        StorageUsage latest = storageCalculationService.getLatestStorageUsage(churchId);

        StorageUsageResponse response = toResponse(latest, DEFAULT_STORAGE_LIMIT_MB);
        return ResponseEntity.ok(response);
    }

    /**
     * Get storage usage history for the authenticated church.
     */
    @GetMapping("/history")
    @RequirePermission({Permission.SUBSCRIPTION_VIEW, Permission.CHURCH_SETTINGS_VIEW})
    @Operation(summary = "Get storage usage history", description = "Returns storage usage history within a date range")
    public ResponseEntity<List<StorageUsageResponse>> getStorageUsageHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        List<StorageUsage> history = storageCalculationService.getStorageUsageHistory(churchId, startDate, endDate);

        List<StorageUsageResponse> responses = history.stream()
                .map(usage -> toResponse(usage, DEFAULT_STORAGE_LIMIT_MB))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Manually trigger storage calculation for the authenticated church.
     * Useful for immediate updates after bulk operations.
     */
    @PostMapping("/calculate")
    @RequirePermission(Permission.SUBSCRIPTION_MANAGE)
    @Operation(summary = "Calculate storage usage", description = "Manually triggers storage calculation for immediate update")
    public ResponseEntity<StorageUsageResponse> calculateStorageUsage(HttpServletRequest request) {
        Long churchId = requestContextUtil.extractChurchId(request);
        StorageUsage usage = storageCalculationService.calculateAndSaveStorageUsage(churchId);

        StorageUsageResponse response = toResponse(usage, DEFAULT_STORAGE_LIMIT_MB);
        return ResponseEntity.ok(response);
    }

    /**
     * Convert StorageUsage entity to response DTO.
     */
    private StorageUsageResponse toResponse(StorageUsage usage, double storageLimitMb) {
        double usagePercentage = (usage.getTotalStorageMb() / storageLimitMb) * 100.0;
        boolean isOverLimit = usage.getTotalStorageMb() > storageLimitMb;

        // Parse JSON breakdowns
        Map<String, Double> fileBreakdown = parseJsonBreakdown(usage.getFileStorageBreakdown());
        Map<String, Double> dbBreakdown = parseJsonBreakdown(usage.getDatabaseStorageBreakdown());

        return StorageUsageResponse.builder()
                .id(usage.getId())
                .churchId(usage.getChurch().getId())
                .churchName(usage.getChurch().getName())
                .fileStorageMb(usage.getFileStorageMb())
                .databaseStorageMb(usage.getDatabaseStorageMb())
                .totalStorageMb(usage.getTotalStorageMb())
                .storageLimitMb(storageLimitMb)
                .usagePercentage(usagePercentage)
                .isOverLimit(isOverLimit)
                .fileStorageBreakdown(fileBreakdown)
                .databaseStorageBreakdown(dbBreakdown)
                .calculatedAt(usage.getCalculatedAt())
                .fileStorageDisplay(formatStorage(usage.getFileStorageMb()))
                .databaseStorageDisplay(formatStorage(usage.getDatabaseStorageMb()))
                .totalStorageDisplay(formatStorage(usage.getTotalStorageMb()))
                .storageLimitDisplay(formatStorage(storageLimitMb))
                .build();
    }

    /**
     * Parse JSON breakdown string to Map.
     */
    private Map<String, Double> parseJsonBreakdown(String json) {
        try {
            if (json == null || json.isEmpty() || json.equals("{}")) {
                return Map.of();
            }
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    /**
     * Format storage size for display.
     * Converts MB to GB if >= 1024 MB.
     */
    private String formatStorage(double mb) {
        if (mb >= 1024.0) {
            double gb = mb / 1024.0;
            return String.format("%.2f GB", gb);
        } else {
            return String.format("%.2f MB", mb);
        }
    }
}
