package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Platform-wide storage statistics for SUPERADMIN dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformStorageStatsResponse {

    /**
     * Total storage used across all churches (MB)
     */
    private Double totalStorageUsedMb;

    /**
     * Total storage used (formatted display string, e.g., "45.2 GB")
     */
    private String totalStorageUsedDisplay;

    /**
     * Average storage per church (MB)
     */
    private Double averageStoragePerChurchMb;

    /**
     * Average storage per church (formatted display string)
     */
    private String averageStoragePerChurchDisplay;

    /**
     * Total file storage (MB)
     */
    private Double totalFileStorageMb;

    /**
     * Total file storage (formatted display string)
     */
    private String totalFileStorageDisplay;

    /**
     * Total database storage (MB)
     */
    private Double totalDatabaseStorageMb;

    /**
     * Total database storage (formatted display string)
     */
    private String totalDatabaseStorageDisplay;

    /**
     * Number of churches using storage
     */
    private Integer churchesWithStorage;

    /**
     * Total number of churches
     */
    private Integer totalChurches;

    /**
     * Storage growth over last 30 days (MB)
     */
    private Double storageGrowth30dMb;

    /**
     * Storage growth over last 30 days (percentage)
     */
    private Double storageGrowth30dPercent;

    /**
     * Highest storage user church ID
     */
    private Long highestStorageChurchId;

    /**
     * Highest storage user church name
     */
    private String highestStorageChurchName;

    /**
     * Highest storage amount (MB)
     */
    private Double highestStorageAmountMb;

    /**
     * Highest storage amount (formatted display)
     */
    private String highestStorageAmountDisplay;
}
