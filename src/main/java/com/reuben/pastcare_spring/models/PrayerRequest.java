package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PrayerRequest entity for member prayer request submission
 */
@Entity
@Table(name = "prayer_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PrayerRequest extends TenantBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrayerRequestCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrayerRequestPriority priority = PrayerRequestPriority.NORMAL;

    @Column(nullable = false)
    private Boolean isAnonymous = false;

    @Column(nullable = false)
    private Boolean isUrgent = false;

    @Column(nullable = false)
    private Boolean isPublic = false; // Can be shared with prayer team/church

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrayerRequestStatus status = PrayerRequestStatus.PENDING;

    @Column
    private LocalDateTime answeredAt;

    @Column(columnDefinition = "TEXT")
    private String testimony; // How prayer was answered

    @Column
    private LocalDateTime expiresAt; // Auto-archive after this date
}
