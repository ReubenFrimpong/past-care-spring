package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.SecurityStatsResponse;
import com.reuben.pastcare_spring.dtos.SecurityViolationResponse;
import com.reuben.pastcare_spring.exceptions.TenantViolationException;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.SecurityAuditLog;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.SecurityAuditLogRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for monitoring and logging security violations.
 *
 * <p>Tracks cross-tenant access attempts, security violations, and suspicious activity.
 * Provides audit trail for compliance and security analysis.
 *
 * <p>Features:
 * <ul>
 *   <li>Log all TenantViolationException occurrences</li>
 *   <li>Track patterns of suspicious behavior</li>
 *   <li>Provide security analytics and reporting</li>
 *   <li>Alert on threshold violations</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityMonitoringService {

    private final SecurityAuditLogRepository auditLogRepository;
    private final ChurchRepository churchRepository;
    private final UserRepository userRepository;

    /**
     * Log a tenant violation attempt.
     * Called by GlobalExceptionHandler when TenantViolationException occurs.
     */
    @Transactional
    public void logTenantViolation(TenantViolationException exception) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                .userId(exception.getUserId())
                .attemptedChurchId(exception.getAttemptedChurchId())
                .actualChurchId(exception.getActualChurchId())
                .resourceType(exception.getResourceType())
                .violationType("CROSS_TENANT_ACCESS")
                .message(exception.getMessage())
                .severity("HIGH")
                .ipAddress(getCurrentIpAddress())
                .userAgent(getCurrentUserAgent())
                .timestamp(LocalDateTime.now())
                .build();

            auditLogRepository.save(auditLog);
            log.info("Security violation logged: User {} attempted to access {} from church {} (actual church: {})",
                exception.getUserId(), exception.getResourceType(),
                exception.getAttemptedChurchId(), exception.getActualChurchId());

            // Check if this user has multiple violations
            checkViolationThreshold(exception.getUserId());

        } catch (Exception e) {
            // Don't fail the request if logging fails
            log.error("Failed to log security violation: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if a user has exceeded violation threshold.
     * Alert if suspicious pattern detected.
     */
    private void checkViolationThreshold(Long userId) {
        // Count violations in last 24 hours
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        long recentViolations = auditLogRepository.countByUserIdAndTimestampAfter(userId, yesterday);

        if (recentViolations >= 5) {
            log.warn("SECURITY ALERT: User {} has {} cross-tenant access attempts in last 24 hours",
                userId, recentViolations);
            // TODO: Send email alert to security team
            // TODO: Consider auto-suspending user account
        }
    }

    /**
     * Get all security violations for a specific user.
     */
    @Transactional(readOnly = true)
    public List<SecurityAuditLog> getUserViolations(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Get all security violations for a specific church.
     */
    @Transactional(readOnly = true)
    public List<SecurityAuditLog> getChurchViolations(Long churchId) {
        return auditLogRepository.findByActualChurchIdOrderByTimestampDesc(churchId);
    }

    /**
     * Get recent security violations (last 7 days).
     */
    @Transactional(readOnly = true)
    public List<SecurityAuditLog> getRecentViolations() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return auditLogRepository.findByTimestampAfterOrderByTimestampDesc(weekAgo);
    }

    /**
     * Get security statistics.
     */
    @Transactional(readOnly = true)
    public SecurityStats getSecurityStats() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);

        long last24Hours = auditLogRepository.countByTimestampAfter(yesterday);
        long last7Days = auditLogRepository.countByTimestampAfter(weekAgo);
        long last30Days = auditLogRepository.countByTimestampAfter(monthAgo);
        long totalViolations = auditLogRepository.count();

        return SecurityStats.builder()
            .violationsLast24Hours(last24Hours)
            .violationsLast7Days(last7Days)
            .violationsLast30Days(last30Days)
            .totalViolations(totalViolations)
            .build();
    }

    /**
     * Get enriched security statistics with all fields.
     */
    @Transactional(readOnly = true)
    public SecurityStatsResponse getEnrichedSecurityStats() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);

        long last24Hours = auditLogRepository.countByTimestampAfter(yesterday);
        long last7Days = auditLogRepository.countByTimestampAfter(weekAgo);
        long last30Days = auditLogRepository.countByTimestampAfter(monthAgo);
        long totalViolations = auditLogRepository.count();

        // Get distinct affected churches and users
        List<SecurityAuditLog> allViolations = auditLogRepository.findAll();
        long affectedChurches = allViolations.stream()
            .map(SecurityAuditLog::getActualChurchId)
            .distinct()
            .count();
        long affectedUsers = allViolations.stream()
            .map(SecurityAuditLog::getUserId)
            .distinct()
            .count();

        // Find most common violation type
        Map<String, Long> violationTypeCounts = allViolations.stream()
            .collect(Collectors.groupingBy(
                v -> v.getViolationType() != null ? v.getViolationType() : "UNKNOWN",
                Collectors.counting()
            ));
        String mostCommonType = violationTypeCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("NONE");

        // Count critical violations
        long criticalViolations = allViolations.stream()
            .filter(v -> "CRITICAL".equals(v.getSeverity()))
            .count();

        return SecurityStatsResponse.builder()
            .totalViolations(totalViolations)
            .violationsLast24h(last24Hours)
            .violationsLast7d(last7Days)
            .violationsLast30d(last30Days)
            .affectedChurches(affectedChurches)
            .affectedUsers(affectedUsers)
            .mostCommonViolationType(mostCommonType)
            .criticalViolations(criticalViolations)
            .build();
    }

    /**
     * Get enriched violations with user and church names.
     */
    @Transactional(readOnly = true)
    public List<SecurityViolationResponse> getEnrichedViolations(List<SecurityAuditLog> violations) {
        // Fetch all users and churches in batch
        Map<Long, User> usersById = new HashMap<>();
        Map<Long, Church> churchesById = new HashMap<>();

        violations.stream().map(SecurityAuditLog::getUserId).distinct()
            .forEach(userId -> {
                if (userId != null) {
                    userRepository.findById(userId).ifPresent(u -> usersById.put(userId, u));
                }
            });

        violations.stream().flatMap(v -> List.of(v.getActualChurchId(), v.getAttemptedChurchId()).stream())
            .distinct()
            .forEach(churchId -> {
                if (churchId != null) {
                    churchRepository.findById(churchId).ifPresent(c -> churchesById.put(churchId, c));
                }
            });

        return violations.stream()
            .map(violation -> {
                User user = usersById.get(violation.getUserId());
                Church actualChurch = churchesById.get(violation.getActualChurchId());
                Church attemptedChurch = churchesById.get(violation.getAttemptedChurchId());

                return SecurityViolationResponse.builder()
                    .id(violation.getId())
                    .userId(violation.getUserId())
                    .userName(user != null ? user.getName() : "Unknown User")
                    .userEmail(user != null ? user.getEmail() : "unknown@unknown.com")
                    .churchId(violation.getActualChurchId())
                    .churchName(actualChurch != null ? actualChurch.getName() : "Unknown Church")
                    .attemptedChurchId(violation.getAttemptedChurchId())
                    .attemptedChurchName(attemptedChurch != null ? attemptedChurch.getName() : "Unknown Church")
                    .violationType(violation.getViolationType())
                    .violationMessage(violation.getMessage())
                    .endpoint(violation.getResourceType())  // Using resourceType as endpoint placeholder
                    .httpMethod("GET")  // Default value, could be enhanced later
                    .ipAddress(violation.getIpAddress())
                    .userAgent(violation.getUserAgent())
                    .timestamp(violation.getTimestamp())
                    .severity(violation.getSeverity())
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Get enriched recent violations.
     */
    @Transactional(readOnly = true)
    public List<SecurityViolationResponse> getEnrichedRecentViolations(int limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<SecurityAuditLog> violations = auditLogRepository.findByTimestampAfterOrderByTimestampDesc(weekAgo);

        // Apply limit
        if (violations.size() > limit) {
            violations = violations.subList(0, limit);
        }

        return getEnrichedViolations(violations);
    }

    /**
     * Get enriched user violations.
     */
    @Transactional(readOnly = true)
    public List<SecurityViolationResponse> getEnrichedUserViolations(Long userId) {
        List<SecurityAuditLog> violations = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
        return getEnrichedViolations(violations);
    }

    /**
     * Get enriched church violations.
     */
    @Transactional(readOnly = true)
    public List<SecurityViolationResponse> getEnrichedChurchViolations(Long churchId) {
        List<SecurityAuditLog> violations = auditLogRepository.findByActualChurchIdOrderByTimestampDesc(churchId);
        return getEnrichedViolations(violations);
    }

    /**
     * Get current request IP address (placeholder).
     * Should be populated from HttpServletRequest in real implementation.
     */
    private String getCurrentIpAddress() {
        // TODO: Get from RequestContextHolder or HttpServletRequest
        return "unknown";
    }

    /**
     * Get current request User-Agent (placeholder).
     * Should be populated from HttpServletRequest in real implementation.
     */
    private String getCurrentUserAgent() {
        // TODO: Get from RequestContextHolder or HttpServletRequest
        return "unknown";
    }

    /**
     * DTO for security statistics.
     */
    @lombok.Data
    @lombok.Builder
    public static class SecurityStats {
        private long violationsLast24Hours;
        private long violationsLast7Days;
        private long violationsLast30Days;
        private long totalViolations;
    }
}
