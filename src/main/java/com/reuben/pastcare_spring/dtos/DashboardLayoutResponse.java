package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.DashboardLayout;

import java.time.LocalDateTime;

/**
 * DTO for dashboard layout information returned to frontend.
 * Dashboard Phase 2.1: Custom Layouts MVP
 */
public record DashboardLayoutResponse(
    Long id,
    String layoutName,
    Boolean isDefault,
    String layoutConfig,  // JSON string
    LocalDateTime updatedAt
) {
    /**
     * Convert DashboardLayout entity to DashboardLayoutResponse DTO
     */
    public static DashboardLayoutResponse fromEntity(DashboardLayout layout) {
        return new DashboardLayoutResponse(
            layout.getId(),
            layout.getLayoutName(),
            layout.getIsDefault(),
            layout.getLayoutConfig(),
            layout.getUpdatedAt()
        );
    }
}
