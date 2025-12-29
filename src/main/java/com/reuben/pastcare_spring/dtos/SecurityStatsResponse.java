package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for security statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityStatsResponse {
    private long totalViolations;
    private long violationsLast24h;
    private long violationsLast7d;
    private long violationsLast30d;
    private long affectedChurches;
    private long affectedUsers;
    private String mostCommonViolationType;
    private long criticalViolations;
}
