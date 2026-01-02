package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Partnership codes allow churches to activate grace periods.
 * These codes can be generated for partners, promotions, or special events.
 */
@Entity
@Table(name = "partnership_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartnershipCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer gracePeriodDays;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = true)
    private LocalDateTime expiresAt;

    @Column(nullable = true)
    private Integer maxUses;

    @Column(nullable = true)
    private Integer maxUsesPerChurch;

    @Column(nullable = false)
    private Integer currentUses = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if the code is valid and can be used
     */
    public boolean isValid() {
        if (!isActive) {
            return false;
        }

        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }

        if (maxUses != null && currentUses >= maxUses) {
            return false;
        }

        return true;
    }

    /**
     * Increment usage count
     */
    public void incrementUsage() {
        this.currentUses++;
    }
}
