package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.dtos.SecurityStatsResponse;
import com.reuben.pastcare_spring.dtos.SecurityViolationResponse;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.services.SecurityMonitoringService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for security monitoring and audit logs.
 * Only accessible by SUPERADMIN users.
 */
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@Tag(name = "Security Monitoring", description = "Security audit and violation tracking (SUPERADMIN only)")
public class SecurityMonitoringController {

    private final SecurityMonitoringService securityMonitoringService;
    private final RequestContextUtil requestContextUtil;

    /**
     * Get security statistics.
     * Platform admin only.
     */
    @GetMapping("/stats")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    @Operation(summary = "Get security statistics", description = "Returns violation counts for various time periods")
    public ResponseEntity<SecurityStatsResponse> getSecurityStats() {
        SecurityStatsResponse stats = securityMonitoringService.getEnrichedSecurityStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get recent security violations (last 7 days).
     * Platform admin only.
     */
    @GetMapping("/violations/recent")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    @Operation(summary = "Get recent violations", description = "Returns violations from last 7 days with enriched data")
    public ResponseEntity<List<SecurityViolationResponse>> getRecentViolations(
            @RequestParam(defaultValue = "100") int limit) {
        List<SecurityViolationResponse> violations = securityMonitoringService.getEnrichedRecentViolations(limit);
        return ResponseEntity.ok(violations);
    }

    /**
     * Get violations for a specific user.
     * Platform admin only.
     */
    @GetMapping("/violations/user/{userId}")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    @Operation(summary = "Get user violations", description = "Returns all violations for a specific user")
    public ResponseEntity<List<SecurityViolationResponse>> getUserViolations(@PathVariable Long userId) {
        List<SecurityViolationResponse> violations = securityMonitoringService.getEnrichedUserViolations(userId);
        return ResponseEntity.ok(violations);
    }

    /**
     * Get violations for a specific church.
     * Church admins can view their own church's violations.
     * Platform admin can view any church.
     */
    @GetMapping("/violations/church/{churchId}")
    @RequirePermission({Permission.PLATFORM_ACCESS, Permission.CHURCH_SETTINGS_VIEW})
    @Operation(summary = "Get church violations", description = "Returns all violations for a specific church")
    public ResponseEntity<List<SecurityViolationResponse>> getChurchViolations(
            @PathVariable Long churchId,
            HttpServletRequest request) {

        // Validate church access (unless platform admin)
        requestContextUtil.extractChurchId(request); // This will validate access

        List<SecurityViolationResponse> violations = securityMonitoringService.getEnrichedChurchViolations(churchId);
        return ResponseEntity.ok(violations);
    }
}
