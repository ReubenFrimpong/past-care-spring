package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for pre-built report type information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportTypeInfo {
    private ReportType reportType;
    private String displayName;
    private String description;
    private String category;
    private String icon; // Icon class name for frontend
}
