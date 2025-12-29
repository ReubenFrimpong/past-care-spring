package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.ExecutionStatus;
import com.reuben.pastcare_spring.enums.ReportFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for report execution history response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportExecutionResponse {
    private Long id;
    private String reportName;
    private LocalDateTime executionDate;
    private String executedByName;
    private ReportFormat format;
    private String outputFileUrl;
    private String outputFileName;
    private Long fileSizeBytes;
    private ExecutionStatus status;
    private String errorMessage;
    private Integer rowCount;
    private Long executionTimeMs;
}
