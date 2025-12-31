package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Storage summary for a single church (used in platform admin).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChurchStorageSummaryResponse {

    /**
     * Church ID
     */
    private Long churchId;

    /**
     * Church name
     */
    private String churchName;

    /**
     * Church active status
     */
    private Boolean active;

    /**
     * Total storage used (MB)
     */
    private Double totalStorageMb;

    /**
     * Total storage used (formatted display string)
     */
    private String totalStorageDisplay;

    /**
     * File storage (MB)
     */
    private Double fileStorageMb;

    /**
     * File storage (formatted display string)
     */
    private String fileStorageDisplay;

    /**
     * Database storage (MB)
     */
    private Double databaseStorageMb;

    /**
     * Database storage (formatted display string)
     */
    private String databaseStorageDisplay;

    /**
     * Storage limit (MB)
     */
    private Double storageLimitMb;

    /**
     * Storage limit (formatted display string)
     */
    private String storageLimitDisplay;

    /**
     * Usage percentage (0-100)
     */
    private Double usagePercentage;

    /**
     * Is over storage limit
     */
    private Boolean isOverLimit;

    /**
     * When storage was last calculated
     */
    private LocalDateTime lastCalculated;

    /**
     * Number of members
     */
    private Integer memberCount;

    /**
     * Number of users
     */
    private Integer userCount;
}
