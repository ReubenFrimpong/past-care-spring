package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a pastoral care need
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "care_needs", indexes = {
    @Index(name = "idx_care_need_member", columnList = "member_id"),
    @Index(name = "idx_care_need_status", columnList = "status"),
    @Index(name = "idx_care_need_priority", columnList = "priority"),
    @Index(name = "idx_care_need_assigned_to", columnList = "assigned_to_user_id")
})
@Data
public class CareNeed extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CareNeedType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CareNeedPriority priority = CareNeedPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CareNeedStatus status = CareNeedStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column
    private LocalDateTime dueDate;

    @Column
    private LocalDateTime resolvedDate;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(nullable = false)
    private Boolean followUpRequired = false;

    @Column
    private LocalDateTime followUpDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FollowUpStatus followUpStatus;

    @ElementCollection
    @CollectionTable(name = "care_need_tags", joinColumns = @JoinColumn(name = "care_need_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();
}
