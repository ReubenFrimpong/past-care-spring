package com.reuben.pastcare_spring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.reuben.pastcare_spring.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Invitation code entity for controlled church registration.
 * Allows churches to generate codes for new member registration.
 */
@Entity
@Table(name = "invitation_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The church that created this invitation code.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;

    /**
     * Unique invitation code (8-12 characters).
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Optional description/note for the code.
     */
    @Column(length = 255)
    private String description;

    /**
     * User who created this invitation code.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    /**
     * Maximum number of times this code can be used.
     * Null = unlimited uses.
     */
    @Column
    private Integer maxUses;

    /**
     * Current number of times this code has been used.
     */
    @Column(nullable = false)
    private Integer usedCount = 0;

    /**
     * Expiration date for this code.
     * Null = never expires.
     */
    @Column
    private LocalDateTime expiresAt;

    /**
     * Whether this code is currently active.
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    /**
     * Role to assign to users who register with this code.
     * Defaults to MEMBER.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Role defaultRole = Role.MEMBER;

    /**
     * Date and time when the code was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Date and time when the code was last used.
     */
    @Column
    private LocalDateTime lastUsedAt;

    /**
     * Check if code is still valid for use.
     */
    public boolean isValid() {
        // Must be active
        if (!isActive) {
            return false;
        }

        // Check expiration
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }

        // Check usage limit
        if (maxUses != null && usedCount >= maxUses) {
            return false;
        }

        return true;
    }

    /**
     * Increment usage count.
     */
    public void incrementUsage() {
        this.usedCount++;
        this.lastUsedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.usedCount == null) {
            this.usedCount = 0;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.defaultRole == null) {
            this.defaultRole = Role.MEMBER;
        }
    }

    // ============ JSON Properties for Frontend ============

    /**
     * Get church ID for JSON serialization.
     */
    @JsonProperty("churchId")
    public Long getChurchId() {
        return church != null ? church.getId() : null;
    }

    /**
     * Get creator user ID for JSON serialization.
     */
    @JsonProperty("createdById")
    public Long getCreatedById() {
        return createdBy != null ? createdBy.getId() : null;
    }

    /**
     * Get creator name for JSON serialization.
     */
    @JsonProperty("createdByName")
    public String getCreatedByName() {
        return createdBy != null ? createdBy.getName() : null;
    }
}
