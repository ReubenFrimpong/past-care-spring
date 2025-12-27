package com.reuben.pastcare_spring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a church event.
 * Supports various event types including services, conferences, outreach, social events, etc.
 * Includes support for recurring events, multi-location, registration management, and capacity control.
 */
@Entity
@Table(name = "events")
@SQLDelete(sql = "UPDATE events SET deleted_at = NOW() WHERE id = ?")
@FilterDef(name = "deletedEventFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedEventFilter", condition = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tenant isolation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    @JsonIgnore
    private Church church;

    // Basic information
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    // Date and time
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "Africa/Nairobi";

    // Location details
    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 50)
    private EventLocationType locationType;

    @Column(name = "physical_location", length = 500)
    private String physicalLocation;

    @Column(name = "virtual_link", length = 1000)
    private String virtualLink;

    @Column(name = "virtual_platform", length = 100)
    private String virtualPlatform;

    // Geographic location (for physical events)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    @JsonIgnore
    private Location location;

    // Registration management
    @Column(name = "requires_registration", nullable = false)
    @Builder.Default
    private Boolean requiresRegistration = false;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "current_registrations", nullable = false)
    @Builder.Default
    private Integer currentRegistrations = 0;

    @Column(name = "allow_waitlist")
    @Builder.Default
    private Boolean allowWaitlist = false;

    @Column(name = "auto_approve_registrations")
    @Builder.Default
    private Boolean autoApproveRegistrations = true;

    // Visibility and access control
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 50)
    @Builder.Default
    private EventVisibility visibility = EventVisibility.PUBLIC;

    // Recurrence support
    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_pattern", length = 50)
    private RecurrencePattern recurrencePattern;

    @Column(name = "recurrence_end_date")
    private LocalDate recurrenceEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_event_id")
    @JsonIgnore
    private Event parentEvent;

    @OneToMany(mappedBy = "parentEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Event> childEvents = new ArrayList<>();

    // Organizer information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_organizer_id")
    @JsonIgnore
    private Member primaryOrganizer;

    // Additional information
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "reminder_sent")
    @Builder.Default
    private Boolean reminderSent = false;

    @Column(name = "reminder_days_before")
    @Builder.Default
    private Integer reminderDaysBefore = 1;

    // Status tracking
    @Column(name = "is_cancelled")
    @Builder.Default
    private Boolean isCancelled = false;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by_id")
    @JsonIgnore
    private User cancelledBy;

    // Relationships
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<EventRegistration> registrations = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<EventOrganizer> organizers = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<EventTag> tags = new ArrayList<>();

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
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
        if (requiresRegistration == null) {
            requiresRegistration = false;
        }
        if (currentRegistrations == null) {
            currentRegistrations = 0;
        }
        if (allowWaitlist == null) {
            allowWaitlist = false;
        }
        if (autoApproveRegistrations == null) {
            autoApproveRegistrations = true;
        }
        if (visibility == null) {
            visibility = EventVisibility.PUBLIC;
        }
        if (isRecurring == null) {
            isRecurring = false;
        }
        if (reminderSent == null) {
            reminderSent = false;
        }
        if (reminderDaysBefore == null) {
            reminderDaysBefore = 1;
        }
        if (isCancelled == null) {
            isCancelled = false;
        }
        if (timezone == null || timezone.isEmpty()) {
            timezone = "Africa/Nairobi";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods

    /**
     * Check if event is currently ongoing
     */
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return !isCancelled && now.isAfter(startDate) && now.isBefore(endDate);
    }

    /**
     * Check if event is upcoming
     */
    public boolean isUpcoming() {
        return !isCancelled && LocalDateTime.now().isBefore(startDate);
    }

    /**
     * Check if event has ended
     */
    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(endDate);
    }

    /**
     * Check if event is at full capacity
     */
    public boolean isAtCapacity() {
        return maxCapacity != null && currentRegistrations >= maxCapacity;
    }

    /**
     * Check if registration is open
     */
    public boolean isRegistrationOpen() {
        if (!requiresRegistration || isCancelled) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (registrationDeadline != null && now.isAfter(registrationDeadline)) {
            return false;
        }
        return !isAtCapacity() || allowWaitlist;
    }

    /**
     * Check if event requires physical location
     */
    public boolean requiresPhysicalLocation() {
        return locationType != null && locationType.requiresPhysicalAddress();
    }

    /**
     * Check if event requires virtual link
     */
    public boolean requiresVirtualLink() {
        return locationType != null && locationType.requiresVirtualLink();
    }

    /**
     * Increment registration count
     */
    public void incrementRegistrations() {
        if (currentRegistrations == null) {
            currentRegistrations = 0;
        }
        currentRegistrations++;
    }

    /**
     * Decrement registration count
     */
    public void decrementRegistrations() {
        if (currentRegistrations != null && currentRegistrations > 0) {
            currentRegistrations--;
        }
    }

    /**
     * Cancel the event
     */
    public void cancel(String reason, User cancelledByUser) {
        this.isCancelled = true;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledBy = cancelledByUser;
    }

    /**
     * Get available capacity
     */
    public Integer getAvailableCapacity() {
        if (maxCapacity == null) {
            return null;
        }
        return Math.max(0, maxCapacity - currentRegistrations);
    }
}
