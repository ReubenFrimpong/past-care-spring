package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

/**
 * Complaint entity for tracking member complaints and feedback.
 * Supports multi-tenant architecture with church_id filtering.
 * Note: @FilterDef is defined in TenantBaseEntity - only @Filter needed here.
 */
@Entity
@Table(name = "complaints")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Filter(name = "churchFilter", condition = "church_id = :churchId")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the church this complaint belongs to (multi-tenant).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;

    /**
     * User who submitted the complaint.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private User submittedBy;

    /**
     * Category of the complaint.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ComplaintCategory category;

    /**
     * Subject/title of the complaint.
     */
    @Column(nullable = false, length = 200)
    private String subject;

    /**
     * Detailed description of the complaint.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Current status of the complaint.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintStatus status = ComplaintStatus.SUBMITTED;

    /**
     * Priority level of the complaint.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintPriority priority = ComplaintPriority.MEDIUM;

    /**
     * Admin/staff member assigned to handle this complaint.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    /**
     * Admin response to the complaint.
     */
    @Column(columnDefinition = "TEXT")
    private String adminResponse;

    /**
     * Date and time when the complaint was submitted.
     */
    @Column(nullable = false)
    private LocalDateTime submittedAt;

    /**
     * Date and time when the complaint was last updated.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Date and time when the complaint was resolved.
     */
    @Column
    private LocalDateTime resolvedAt;

    /**
     * Whether the submitter wants to remain anonymous.
     */
    @Column(nullable = false)
    private Boolean isAnonymous = false;

    /**
     * Contact email for follow-up (optional, if different from user email).
     */
    @Column(length = 255)
    private String contactEmail;

    /**
     * Contact phone for follow-up (optional).
     */
    @Column(length = 50)
    private String contactPhone;

    /**
     * Internal notes for staff/admin (not visible to complainant).
     */
    @Column(columnDefinition = "TEXT")
    private String internalNotes;

    /**
     * Tags for categorizing and filtering complaints.
     */
    @Column(length = 500)
    private String tags;

    /**
     * Complaint categories.
     */
    public enum ComplaintCategory {
        GENERAL,           // General complaint
        SERVICE,           // Church service related
        FACILITY,          // Facility/building issues
        STAFF,             // Staff behavior or performance
        FINANCIAL,         // Financial or donation concerns
        MINISTRY,          // Ministry program issues
        SAFEGUARDING,      // Child/vulnerable adult protection
        DISCRIMINATION,    // Discrimination or harassment
        OTHER              // Other category
    }

    /**
     * Complaint status lifecycle.
     */
    public enum ComplaintStatus {
        SUBMITTED,         // Initial submission
        UNDER_REVIEW,      // Being reviewed by admin
        IN_PROGRESS,       // Being actively worked on
        PENDING_RESPONSE,  // Waiting for complainant response
        RESOLVED,          // Complaint has been resolved
        CLOSED,            // Complaint closed (no further action)
        ESCALATED          // Escalated to higher authority
    }

    /**
     * Complaint priority levels.
     */
    public enum ComplaintPriority {
        LOW,               // Low priority
        MEDIUM,            // Medium priority
        HIGH,              // High priority
        URGENT             // Urgent - requires immediate attention
    }

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ComplaintStatus.SUBMITTED;
        }
        if (this.priority == null) {
            this.priority = ComplaintPriority.MEDIUM;
        }
        if (this.isAnonymous == null) {
            this.isAnonymous = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.status == ComplaintStatus.RESOLVED || this.status == ComplaintStatus.CLOSED) {
            if (this.resolvedAt == null) {
                this.resolvedAt = LocalDateTime.now();
            }
        }
    }
}
