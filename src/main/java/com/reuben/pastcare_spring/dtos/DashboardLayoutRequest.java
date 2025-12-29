package com.reuben.pastcare_spring.dtos;

/**
 * DTO for creating/updating dashboard layouts.
 * Dashboard Phase 2.1: Custom Layouts MVP
 */
public record DashboardLayoutRequest(
    String layoutName,
    String layoutConfig  // JSON string
) {}
