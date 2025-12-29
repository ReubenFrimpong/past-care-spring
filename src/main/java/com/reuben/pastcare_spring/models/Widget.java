package com.reuben.pastcare_spring.models;

import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.enums.WidgetCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Widget entity representing a dashboard widget in the catalog.
 * Dashboard Phase 2.1: Custom Layouts MVP
 *
 * Widgets are system-defined UI components that can be added to user dashboards.
 * Each widget has metadata about its size, category, and role requirements.
 */
@Entity
@Table(name = "widgets")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Widget extends BaseEntity {

    /**
     * Unique key identifying this widget (e.g., "stats_overview", "birthdays")
     */
    @Column(nullable = false, unique = true, length = 100)
    private String widgetKey;

    /**
     * Display name for the widget
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Description of the widget's purpose and data
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Category for organizing widgets
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private WidgetCategory category;

    /**
     * Icon name for UI display (optional)
     */
    @Column(length = 50)
    private String icon;

    /**
     * Default width in grid units (1-4)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer defaultWidth = 1;

    /**
     * Default height in grid units (1-3)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer defaultHeight = 1;

    /**
     * Minimum width in grid units
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer minWidth = 1;

    /**
     * Minimum height in grid units
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer minHeight = 1;

    /**
     * Required role to view this widget (NULL = available to all)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role requiredRole;

    /**
     * Whether this widget is active and available for use
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
