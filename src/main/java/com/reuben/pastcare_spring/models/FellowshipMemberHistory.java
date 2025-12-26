package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Tracks fellowship membership changes for retention analysis
 * Records when members join or leave fellowships
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "fellowship_member_history",
    indexes = {
        @Index(name = "idx_fmh_fellowship", columnList = "fellowship_id"),
        @Index(name = "idx_fmh_member", columnList = "member_id"),
        @Index(name = "idx_fmh_date", columnList = "effectiveDate"),
        @Index(name = "idx_fmh_action", columnList = "action")
    }
)
@Data
public class FellowshipMemberHistory extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fellowship_id", nullable = false)
    private Fellowship fellowship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FellowshipMemberAction action;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id")
    private User recordedBy;
}
