package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.Widget;

/**
 * DTO for widget information returned to frontend.
 * Dashboard Phase 2.1: Custom Layouts MVP
 */
public record WidgetResponse(
    Long id,
    String widgetKey,
    String name,
    String description,
    String category,
    String icon,
    Integer defaultWidth,
    Integer defaultHeight,
    Integer minWidth,
    Integer minHeight,
    String requiredRole,
    Boolean isActive
) {
    /**
     * Convert Widget entity to WidgetResponse DTO
     */
    public static WidgetResponse fromEntity(Widget widget) {
        return new WidgetResponse(
            widget.getId(),
            widget.getWidgetKey(),
            widget.getName(),
            widget.getDescription(),
            widget.getCategory().name(),
            widget.getIcon(),
            widget.getDefaultWidth(),
            widget.getDefaultHeight(),
            widget.getMinWidth(),
            widget.getMinHeight(),
            widget.getRequiredRole() != null ? widget.getRequiredRole().name() : null,
            widget.getIsActive()
        );
    }
}
