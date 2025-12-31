package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for churches with pending data deletion.
 * Used by SUPERADMIN to monitor and manage data retention.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingDeletionResponse {

    /**
     * Church ID
     */
    private Long churchId;

    /**
     * Church name
     */
    private String churchName;

    /**
     * Subscription status (should be SUSPENDED)
     */
    private String status;

    /**
     * When subscription was suspended
     */
    private LocalDateTime suspendedAt;

    /**
     * Date when data will be deleted
     */
    private LocalDate dataRetentionEndDate;

    /**
     * Days until deletion (negative if overdue)
     */
    private Long daysUntilDeletion;

    /**
     * When deletion warning was sent
     */
    private LocalDateTime deletionWarningSentAt;

    /**
     * Days until warning should be sent (7 days before deletion)
     */
    private Long daysUntilWarning;

    /**
     * Has warning been sent?
     */
    private Boolean warningSent;

    /**
     * Days extended beyond 90 days
     */
    private Integer retentionExtensionDays;

    /**
     * SUPERADMIN note for extension
     */
    private String retentionExtensionNote;

    /**
     * Urgency level based on days remaining
     */
    private String urgencyLevel;

    /**
     * Church email contact
     */
    private String churchEmail;

    /**
     * Number of admin users for this church
     */
    private Integer adminCount;
}
