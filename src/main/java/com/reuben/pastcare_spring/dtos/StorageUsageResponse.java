package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for storage usage information.
 * Returned to clients showing their current storage consumption.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageUsageResponse {

    private Long id;
    private Long churchId;
    private String churchName;

    /**
     * File storage in megabytes (MB).
     */
    private Double fileStorageMb;

    /**
     * Database storage in megabytes (MB).
     */
    private Double databaseStorageMb;

    /**
     * Total storage in megabytes (MB).
     */
    private Double totalStorageMb;

    /**
     * Storage limit based on subscription (in MB).
     * Default: 2048 MB (2 GB)
     */
    private Double storageLimitMb;

    /**
     * Storage usage percentage (0-100).
     */
    private Double usagePercentage;

    /**
     * Is the church over their storage limit?
     */
    private Boolean isOverLimit;

    /**
     * File storage breakdown by category.
     * Example: {"profilePhotos": 50.5, "eventImages": 120.3, "documents": 30.2}
     */
    private Map<String, Double> fileStorageBreakdown;

    /**
     * Database storage breakdown by entity.
     * Example: {"members": 5.2, "donations": 8.5, "events": 3.1}
     */
    private Map<String, Double> databaseStorageBreakdown;

    /**
     * When this storage usage was calculated.
     */
    private LocalDateTime calculatedAt;

    /**
     * Human-readable storage amounts.
     */
    private String fileStorageDisplay;  // e.g., "150.5 MB"
    private String databaseStorageDisplay;  // e.g., "25.3 MB"
    private String totalStorageDisplay;  // e.g., "175.8 MB"
    private String storageLimitDisplay;  // e.g., "2.0 GB"
}
