package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.ReportFormat;
import com.reuben.pastcare_spring.enums.ScheduleFrequency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * DTO for creating or updating a report schedule.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleReportRequest {
    private Long reportId;
    private ScheduleFrequency frequency;
    private Integer dayOfWeek; // 1-7 for weekly
    private Integer dayOfMonth; // 1-31 for monthly
    private LocalTime executionTime;
    private List<String> recipientEmails;
    private ReportFormat format;
    private Boolean isActive;
}
