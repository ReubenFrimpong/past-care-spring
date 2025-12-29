package com.reuben.pastcare_spring.models;

import com.reuben.pastcare_spring.enums.ExecutionStatus;
import com.reuben.pastcare_spring.enums.ReportFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a report execution history record.
 * Tracks when reports are generated, by whom, and the output details.
 */
@Entity
@Table(name = "report_executions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReportExecution extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private ReportSchedule schedule; // Null if manually executed

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by")
    private User executedBy; // Null if scheduled

    @Column(nullable = false)
    private LocalDateTime executionDate;

    @Column(columnDefinition = "TEXT")
    private String parameters; // JSON string of execution parameters

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportFormat format;

    private String outputFileUrl; // S3 URL or file path

    private String outputFileName;

    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    @Column(length = 2000)
    private String errorMessage;

    private Integer rowCount;

    private Long executionTimeMs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;
}
