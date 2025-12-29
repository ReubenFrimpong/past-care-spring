package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.ReportFormat;
import com.reuben.pastcare_spring.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO for generating a report.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateReportRequest {
    private Long reportId;
    private ReportType reportType;
    private ReportFormat format;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, Object> filters; // Dynamic filters
}
