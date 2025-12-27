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
 * Entity representing an organizer for an event.
 * Links members to events with their organizing roles and responsibilities.
 */
@Entity
@Table(name = "event_organizers")
@SQLDelete(sql = "UPDATE event_organizers SET deleted_at = NOW() WHERE id = ?")
@FilterDef(name = "deletedEventOrganizerFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedEventOrganizerFilter", condition = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventOrganizer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tenant isolation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    @JsonIgnore
    private Church church;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnore
    private Member member;

    // Organizer role
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "role", length = 100)
    private String role;

    // Contact preferences
    @Column(name = "is_contact_person")
    @Builder.Default
    private Boolean isContactPerson = false;

    @Column(name = "contact_email", length = 200)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    // Responsibilities
    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    @JsonIgnore
    private User createdBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isPrimary == null) {
            isPrimary = false;
        }
        if (isContactPerson == null) {
            isContactPerson = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods

    /**
     * Get organizer name from member
     */
    public String getOrganizerName() {
        return member != null ? member.getFirstName() + " " + member.getLastName() : "Unknown";
    }

    /**
     * Get effective contact email (custom or member's email)
     */
    public String getEffectiveContactEmail() {
        if (contactEmail != null && !contactEmail.isEmpty()) {
            return contactEmail;
        }
        return member != null ? member.getEmail() : null;
    }

    /**
     * Get effective contact phone (custom or member's phone)
     */
    public String getEffectiveContactPhone() {
        if (contactPhone != null && !contactPhone.isEmpty()) {
            return contactPhone;
        }
        return member != null ? member.getPhoneNumber() : null;
    }

    /**
     * Set as primary organizer
     */
    public void setAsPrimary() {
        this.isPrimary = true;
    }

    /**
     * Remove primary organizer status
     */
    public void removePrimaryStatus() {
        this.isPrimary = false;
    }

    /**
     * Set as contact person
     */
    public void setAsContactPerson(String email, String phone) {
        this.isContactPerson = true;
        this.contactEmail = email;
        this.contactPhone = phone;
    }
}
