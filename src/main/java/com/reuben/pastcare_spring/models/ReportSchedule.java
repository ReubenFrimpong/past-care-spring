package com.reuben.pastcare_spring.models;

import com.reuben.pastcare_spring.enums.ReportFormat;
import com.reuben.pastcare_spring.enums.ScheduleFrequency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a scheduled report generation.
 */
@Entity
@Table(name = "report_schedules")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReportSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleFrequency frequency;

    private Integer dayOfWeek; // 1-7 for weekly (1=Monday, 7=Sunday)

    private Integer dayOfMonth; // 1-31 for monthly

    @Column(nullable = false)
    private LocalTime executionTime;

    @ElementCollection
    @CollectionTable(name = "report_schedule_recipients", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "email")
    private List<String> recipientEmails = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportFormat format;

    private LocalDateTime nextExecutionDate;

    private LocalDateTime lastExecutionDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;
}
