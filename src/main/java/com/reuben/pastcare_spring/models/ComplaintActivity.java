package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

/**
 * Activity log for complaints - tracks all status changes and updates.
 * Provides audit trail for complaint handling.
 * Note: @FilterDef is defined in TenantBaseEntity - only @Filter needed here.
 */
@Entity
@Table(name = "complaint_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Filter(name = "churchFilter", condition = "church_id = :churchId")
public class ComplaintActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the church (for multi-tenant filtering).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;

    /**
     * The complaint this activity belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    /**
     * User who performed the activity (admin/staff).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_user_id", nullable = false)
    private User performedBy;

    /**
     * Type of activity performed.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityType activityType;

    /**
     * Old value before the change (for auditing).
     */
    @Column(length = 1000)
    private String oldValue;

    /**
     * New value after the change.
     */
    @Column(length = 1000)
    private String newValue;

    /**
     * Description/note about the activity.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Date and time when the activity was performed.
     */
    @Column(nullable = false)
    private LocalDateTime performedAt;

    /**
     * Whether this activity is visible to the complainant.
     */
    @Column(nullable = false)
    private Boolean visibleToComplainant = true;

    /**
     * Types of activities that can be performed on a complaint.
     */
    public enum ActivityType {
        CREATED,            // Complaint was created
        STATUS_CHANGED,     // Status was changed
        PRIORITY_CHANGED,   // Priority was changed
        ASSIGNED,           // Complaint was assigned to someone
        UNASSIGNED,         // Complaint was unassigned
        COMMENT_ADDED,      // Comment was added
        RESPONSE_ADDED,     // Admin response was added
        UPDATED,            // General update
        ESCALATED,          // Complaint was escalated
        CLOSED              // Complaint was closed
    }

    @PrePersist
    protected void onCreate() {
        this.performedAt = LocalDateTime.now();
        if (this.visibleToComplainant == null) {
            this.visibleToComplainant = true;
        }
    }
}
