package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating or updating a report.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    private String name;
    private String description;
    private ReportType reportType;
    private String filters; // JSON
    private String fields; // JSON
    private String sorting; // JSON
    private String grouping; // JSON
    private Boolean isTemplate;
    private Boolean isShared;
    private List<Long> sharedWithUserIds;
}
