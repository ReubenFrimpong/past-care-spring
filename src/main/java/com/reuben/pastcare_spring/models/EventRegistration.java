package com.reuben.pastcare_spring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

/**
 * Entity representing a member's registration for an event.
 * Supports approval workflow, waitlist management, and attendance tracking.
 */
@Entity
@Table(name = "event_registrations")
@SQLDelete(sql = "UPDATE event_registrations SET deleted_at = NOW() WHERE id = ?")
@FilterDef(name = "deletedEventRegistrationFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedEventRegistrationFilter", condition = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tenant isolation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    @JsonIgnore
    private Church church;

    // Registration details
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnore
    private Member member;

    // Guest registration support (for non-members)
    @Column(name = "is_guest", nullable = false)
    @Builder.Default
    private Boolean isGuest = false;

    @Column(name = "guest_name", length = 200)
    private String guestName;

    @Column(name = "guest_email", length = 200)
    private String guestEmail;

    @Column(name = "guest_phone", length = 20)
    private String guestPhone;

    // Registration status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    // Guest count
    @Column(name = "number_of_guests", nullable = false)
    @Builder.Default
    private Integer numberOfGuests = 0;

    @Column(name = "guest_names", columnDefinition = "TEXT")
    private String guestNames;

    // Approval workflow
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    @JsonIgnore
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // Attendance tracking (links to existing attendance module)
    @Column(name = "attended")
    @Builder.Default
    private Boolean attended = false;

    @Column(name = "attendance_record_id")
    private Long attendanceRecordId;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    // Waitlist management
    @Column(name = "is_on_waitlist")
    @Builder.Default
    private Boolean isOnWaitlist = false;

    @Column(name = "waitlist_position")
    private Integer waitlistPosition;

    @Column(name = "promoted_from_waitlist_at")
    private LocalDateTime promotedFromWaitlistAt;

    // Communication
    @Column(name = "confirmation_sent")
    @Builder.Default
    private Boolean confirmationSent = false;

    @Column(name = "reminder_sent")
    @Builder.Default
    private Boolean reminderSent = false;

    // QR Code Ticket
    @Column(name = "ticket_code", length = 500, unique = true)
    private String ticketCode;

    // Cancellation
    @Column(name = "is_cancelled")
    @Builder.Default
    private Boolean isCancelled = false;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Notes
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    @JsonIgnore
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    @JsonIgnore
    private User updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (isGuest == null) {
            isGuest = false;
        }
        if (status == null) {
            status = RegistrationStatus.PENDING;
        }
        if (numberOfGuests == null) {
            numberOfGuests = 0;
        }
        if (attended == null) {
            attended = false;
        }
        if (isOnWaitlist == null) {
            isOnWaitlist = false;
        }
        if (confirmationSent == null) {
            confirmationSent = false;
        }
        if (reminderSent == null) {
            reminderSent = false;
        }
        if (isCancelled == null) {
            isCancelled = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods

    /**
     * Approve the registration
     */
    public void approve(User approver) {
        this.status = RegistrationStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = null;
    }

    /**
     * Reject the registration
     */
    public void reject(String reason, User approver) {
        this.status = RegistrationStatus.REJECTED;
        this.rejectionReason = reason;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * Cancel the registration
     */
    public void cancel(String reason) {
        this.isCancelled = true;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
        this.status = RegistrationStatus.CANCELLED;
    }

    /**
     * Mark as attended
     */
    public void markAsAttended() {
        this.attended = true;
        this.status = RegistrationStatus.ATTENDED;
        if (this.checkInTime == null) {
            this.checkInTime = LocalDateTime.now();
        }
    }

    /**
     * Mark as no-show
     */
    public void markAsNoShow() {
        this.attended = false;
        this.status = RegistrationStatus.NO_SHOW;
    }

    /**
     * Promote from waitlist
     */
    public void promoteFromWaitlist() {
        this.isOnWaitlist = false;
        this.promotedFromWaitlistAt = LocalDateTime.now();
        this.waitlistPosition = null;
    }

    /**
     * Get total attendee count (registrant + guests)
     */
    public int getTotalAttendeeCount() {
        return 1 + (numberOfGuests != null ? numberOfGuests : 0);
    }

    /**
     * Check if registration is active
     */
    public boolean isActive() {
        return !isCancelled && (status == RegistrationStatus.PENDING || status == RegistrationStatus.APPROVED);
    }

    /**
     * Get registrant name (member or guest)
     */
    public String getRegistrantName() {
        if (isGuest) {
            return guestName;
        }
        return member != null ? member.getFirstName() + " " + member.getLastName() : "Unknown";
    }

    /**
     * Get registrant email (member or guest)
     */
    public String getRegistrantEmail() {
        if (isGuest) {
            return guestEmail;
        }
        return member != null ? member.getEmail() : null;
    }

    /**
     * Get registrant phone (member or guest)
     */
    public String getRegistrantPhone() {
        if (isGuest) {
            return guestPhone;
        }
        return member != null ? member.getPhoneNumber() : null;
    }
}
