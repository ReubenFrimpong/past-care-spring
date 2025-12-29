package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformStatsResponse {
    private Long totalChurches;
    private Long activeChurches;
    private Long totalUsers;
    private Long activeUsers;
    private Long totalMembers;
    private String totalStorageUsed; // e.g., "45.2 GB"
    private Double averageStoragePerChurch; // in MB
}
