package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for tracking security violations and audit trail.
 *
 * <p>Logs all cross-tenant access attempts and other security violations
 * for compliance, monitoring, and security analysis.
 */
@Entity
@Table(name = "security_audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User ID who attempted the action.
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Church ID that was attempted to be accessed.
     */
    @Column(name = "attempted_church_id")
    private Long attemptedChurchId;

    /**
     * Actual church ID of the user making the request.
     */
    @Column(name = "actual_church_id")
    private Long actualChurchId;

    /**
     * Type and ID of resource that was attempted to be accessed.
     * Example: "Member:123", "Donation:456"
     */
    @Column(name = "resource_type", length = 100)
    private String resourceType;

    /**
     * Type of security violation.
     * Example: "CROSS_TENANT_ACCESS", "PERMISSION_DENIED", "INVALID_TOKEN"
     */
    @Column(name = "violation_type", length = 50)
    private String violationType;

    /**
     * Detailed message about the violation.
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * Severity level: LOW, MEDIUM, HIGH, CRITICAL
     */
    @Column(name = "severity", length = 20)
    private String severity;

    /**
     * IP address of the request.
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent of the request.
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Timestamp when the violation occurred.
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Whether this violation has been reviewed by security team.
     */
    @Column(name = "reviewed")
    private Boolean reviewed = false;

    /**
     * Notes from security team review.
     */
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    /**
     * Timestamp when reviewed.
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * User ID who reviewed this violation.
     */
    @Column(name = "reviewed_by")
    private Long reviewedBy;
}
