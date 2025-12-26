package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Tracks fellowship multiplication events
 * Records when a fellowship "births" a new fellowship through growth and division
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "fellowship_multiplication",
    indexes = {
        @Index(name = "idx_fm_parent", columnList = "parent_fellowship_id"),
        @Index(name = "idx_fm_child", columnList = "child_fellowship_id"),
        @Index(name = "idx_fm_date", columnList = "multiplicationDate")
    }
)
@Data
public class FellowshipMultiplication extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_fellowship_id", nullable = false)
    private Fellowship parentFellowship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_fellowship_id", nullable = false)
    private Fellowship childFellowship;

    @Column(nullable = false)
    private LocalDate multiplicationDate;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column
    private Integer membersTransferred;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id")
    private User recordedBy;
}
