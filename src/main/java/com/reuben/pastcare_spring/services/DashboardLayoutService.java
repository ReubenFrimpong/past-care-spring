package com.reuben.pastcare_spring.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.dtos.DashboardLayoutRequest;
import com.reuben.pastcare_spring.dtos.DashboardLayoutResponse;
import com.reuben.pastcare_spring.dtos.WidgetResponse;
import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.models.DashboardLayout;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.models.Widget;
import com.reuben.pastcare_spring.repositories.DashboardLayoutRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.repositories.WidgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing dashboard layouts and widgets.
 * Dashboard Phase 2.1: Custom Layouts MVP
 */
@Service
@RequiredArgsConstructor
public class DashboardLayoutService {

    private final DashboardLayoutRepository layoutRepository;
    private final WidgetRepository widgetRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get all available widgets for current user (filtered by role)
     */
    public List<WidgetResponse> getAvailableWidgets(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return widgetRepository.findByIsActiveTrue().stream()
            .filter(widget -> isWidgetAvailableForUser(widget, user))
            .map(WidgetResponse::fromEntity)
            .toList();
    }

    /**
     * Check if widget is available for user based on role requirements
     */
    private boolean isWidgetAvailableForUser(Widget widget, User user) {
        if (widget.getRequiredRole() == null) {
            return true;  // Available to all
        }
        // Check if user has required role or higher privilege
        return user.getRole() == widget.getRequiredRole() ||
               user.getRole() == Role.ADMIN ||
               user.getRole() == Role.SUPERADMIN;
    }

    /**
     * Get user's current dashboard layout (or create default if not exists)
     */
    public DashboardLayoutResponse getUserLayout(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return layoutRepository.findByUserAndIsDefaultTrue(user)
            .map(DashboardLayoutResponse::fromEntity)
            .orElseGet(() -> createDefaultLayout(user));
    }

    /**
     * Create default layout for new users
     */
    private DashboardLayoutResponse createDefaultLayout(User user) {
        // Build default layout based on role
        String defaultConfig = buildDefaultLayoutConfig(user.getRole());

        DashboardLayout layout = DashboardLayout.builder()
            .user(user)
            .church(user.getChurch())
            .layoutName("My Dashboard")
            .isDefault(true)
            .layoutConfig(defaultConfig)
            .build();

        DashboardLayout saved = layoutRepository.save(layout);
        return DashboardLayoutResponse.fromEntity(saved);
    }

    /**
     * Build default layout configuration JSON based on user role
     */
    private String buildDefaultLayoutConfig(Role role) {
        // Default layout with all 17 widgets seeded in V47 migration
        // Widget keys match exactly with widgets table
        // Phase 2.2 will provide role-specific templates
        return """
        {
          "version": 1,
          "gridColumns": 4,
          "widgets": [
            {"widgetKey": "stats_overview", "position": {"x": 0, "y": 0}, "size": {"width": 2, "height": 1}, "visible": true},
            {"widgetKey": "pastoral_care", "position": {"x": 2, "y": 0}, "size": {"width": 2, "height": 1}, "visible": true},
            {"widgetKey": "upcoming_events", "position": {"x": 0, "y": 1}, "size": {"width": 2, "height": 1}, "visible": true},
            {"widgetKey": "recent_activities", "position": {"x": 2, "y": 1}, "size": {"width": 2, "height": 1}, "visible": true},
            {"widgetKey": "birthdays_week", "position": {"x": 0, "y": 2}, "size": {"width": 1, "height": 1}, "visible": true},
            {"widgetKey": "anniversaries_month", "position": {"x": 1, "y": 2}, "size": {"width": 1, "height": 1}, "visible": true},
            {"widgetKey": "irregular_attenders", "position": {"x": 2, "y": 2}, "size": {"width": 2, "height": 1}, "visible": true},
            {"widgetKey": "member_growth", "position": {"x": 0, "y": 3}, "size": {"width": 2, "height": 1}, "visible": true},
            {"widgetKey": "location_stats", "position": {"x": 2, "y": 3}, "size": {"width": 2, "height": 1}, "visible": true},
            {"widgetKey": "attendance_summary", "position": {"x": 0, "y": 4}, "size": {"width": 2, "height": 1}, "visible": true},
            {"widgetKey": "service_analytics", "position": {"x": 2, "y": 4}, "size": {"width": 2, "height": 1}, "visible": true},
            {"widgetKey": "top_members", "position": {"x": 0, "y": 5}, "size": {"width": 2, "height": 1}, "visible": true},
            {"widgetKey": "fellowship_health", "position": {"x": 2, "y": 5}, "size": {"width": 2, "height": 1}, "visible": true},
            {"widgetKey": "donation_stats", "position": {"x": 0, "y": 6}, "size": {"width": 2, "height": 1}, "visible": true},
            {"widgetKey": "crisis_stats", "position": {"x": 2, "y": 6}, "size": {"width": 1, "height": 1}, "visible": true},
            {"widgetKey": "counseling_sessions", "position": {"x": 3, "y": 6}, "size": {"width": 1, "height": 1}, "visible": true},
            {"widgetKey": "sms_credits", "position": {"x": 0, "y": 7}, "size": {"width": 1, "height": 1}, "visible": true}
          ]
        }
        """;
    }

    /**
     * Save/update user's dashboard layout
     */
    public DashboardLayoutResponse saveLayout(Long userId, DashboardLayoutRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate JSON structure
        validateLayoutConfig(request.layoutConfig());

        DashboardLayout layout = layoutRepository.findByUserAndIsDefaultTrue(user)
            .orElse(DashboardLayout.builder()
                .user(user)
                .church(user.getChurch())
                .isDefault(true)
                .build());

        layout.setLayoutName(request.layoutName());
        layout.setLayoutConfig(request.layoutConfig());

        DashboardLayout saved = layoutRepository.save(layout);
        return DashboardLayoutResponse.fromEntity(saved);
    }

    /**
     * Validate layout configuration JSON
     */
    private void validateLayoutConfig(String layoutConfig) {
        try {
            objectMapper.readTree(layoutConfig);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid layout configuration JSON: " + e.getMessage());
        }
    }

    /**
     * Reset to default layout
     */
    public DashboardLayoutResponse resetToDefault(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete existing default layout
        layoutRepository.findByUserAndIsDefaultTrue(user)
            .ifPresent(layoutRepository::delete);

        // Create new default layout
        return createDefaultLayout(user);
    }
}
