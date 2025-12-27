package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Counseling session entity for tracking confidential counseling sessions
 * with members. Includes referral support and professional counselor tracking.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "counseling_session")
public class CounselingSession extends TenantBaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "counselor_id", nullable = false)
    private User counselor;

    @ManyToOne
    @JoinColumn(name = "care_need_id")
    private CareNeed careNeed;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String sessionNotes;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private CounselingType type;

    @Column(nullable = false)
    private LocalDateTime sessionDate;

    private Integer durationMinutes;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CounselingStatus status = CounselingStatus.SCHEDULED;

    // Referral fields
    private Boolean isReferralNeeded = false;

    @Column(length = 200)
    private String referredTo; // Professional counselor/therapist name

    @Column(length = 100)
    private String referralOrganization;

    @Column(length = 20)
    private String referralPhone;

    @Column(columnDefinition = "TEXT")
    private String referralNotes;

    private LocalDateTime referralDate;

    // Follow-up
    private Boolean followUpRequired = false;

    private LocalDateTime followUpDate;

    @Column(columnDefinition = "TEXT")
    private String followUpNotes;

    // Confidentiality
    @Column(nullable = false)
    private Boolean isConfidential = true;

    // Outcome
    @Column(columnDefinition = "TEXT")
    private String outcome;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SessionOutcome sessionOutcome;
}
