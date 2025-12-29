package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Dashboard Layout entity representing a user's customized dashboard configuration.
 * Dashboard Phase 2.1: Custom Layouts MVP
 *
 * Each user can have multiple layouts, but only one default layout per church.
 * Layout configuration is stored as JSON in the layout_config TEXT column.
 */
@Entity
@Table(name = "dashboard_layouts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who owns this layout
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The church this layout belongs to (for multi-tenant isolation)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;

    /**
     * Name of the layout (e.g., "My Dashboard", "Analytics View")
     */
    @Column(nullable = false, length = 200)
    @Builder.Default
    private String layoutName = "My Dashboard";

    /**
     * Whether this is the user's default/active layout
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = true;

    /**
     * JSON string containing layout configuration.
     *
     * Structure:
     * {
     *   "version": 1,
     *   "gridColumns": 4,
     *   "widgets": [
     *     {
     *       "widgetKey": "stats_overview",
     *       "position": {"x": 0, "y": 0},
     *       "size": {"width": 2, "height": 1},
     *       "visible": true
     *     }
     *   ]
     * }
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String layoutConfig;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
