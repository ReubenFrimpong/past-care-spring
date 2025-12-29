package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for report information response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private String name;
    private String description;
    private ReportType reportType;
    private Boolean isCustom;
    private Boolean isTemplate;
    private Boolean isShared;
    private String createdByName;
    private LocalDateTime createdAt;
    private Integer executionCount;
    private LocalDateTime lastExecutedAt;
}
