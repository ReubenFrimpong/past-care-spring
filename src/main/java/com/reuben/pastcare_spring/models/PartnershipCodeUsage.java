package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tracks partnership code usage per church to enforce per-church usage limits.
 * Allows codes like "14DAYSFREETRIAL" to be used by multiple churches,
 * but prevents a single church from using the same code multiple times.
 */
@Entity
@Table(name = "partnership_code_usage",
       uniqueConstraints = @UniqueConstraint(columnNames = {"partnership_code_id", "church_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartnershipCodeUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partnership_code_id", nullable = false)
    private Long partnershipCodeId;

    @Column(name = "church_id", nullable = false)
    private Long churchId;

    @Column(nullable = false)
    private LocalDateTime usedAt;

    @Column(nullable = false)
    private Integer gracePeriodDaysGranted;

    @PrePersist
    protected void onCreate() {
        usedAt = LocalDateTime.now();
    }
}
