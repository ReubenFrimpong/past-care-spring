package com.reuben.pastcare_spring.models;

import com.reuben.pastcare_spring.enums.ReportType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a report definition.
 * Can be a pre-built report or a custom user-created report.
 */
@Entity
@Table(name = "reports")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Column(nullable = false)
    private Boolean isCustom = false;

    @Column(columnDefinition = "TEXT")
    private String filters; // JSON string of filter configuration

    @Column(columnDefinition = "TEXT")
    private String fields; // JSON string of selected fields

    @Column(columnDefinition = "TEXT")
    private String sorting; // JSON string of sort configuration

    @Column(columnDefinition = "TEXT")
    private String grouping; // JSON string of grouping configuration

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;

    @Column(nullable = false)
    private Boolean isTemplate = false;

    @Column(nullable = false)
    private Boolean isShared = false;

    @ElementCollection
    @CollectionTable(name = "report_shared_users", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "user_id")
    private List<Long> sharedWithUserIds = new ArrayList<>();
}
