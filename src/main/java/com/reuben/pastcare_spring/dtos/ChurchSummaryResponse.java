package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChurchSummaryResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private Boolean active;
    private Instant createdAt;
    private Integer userCount;
    private Integer memberCount;
    private String storageUsed; // e.g., "1.2 GB"
    private Double storageUsedMB; // in MB for sorting
    private Integer storagePercentage; // 0-100
}
