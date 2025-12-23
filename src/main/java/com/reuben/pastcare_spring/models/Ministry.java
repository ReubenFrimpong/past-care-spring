package com.reuben.pastcare_spring.models;

import com.reuben.pastcare_spring.enums.MinistryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Ministry entity representing church ministries/departments
 * Members can be assigned to ministries based on their skills
 */
@Entity
@Table(name = "ministries", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "church_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ministry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private Member leader;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MinistryStatus status = MinistryStatus.ACTIVE;

    /**
     * Skills that are relevant/required for this ministry
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ministry_skills",
        joinColumns = @JoinColumn(name = "ministry_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> requiredSkills = new HashSet<>();

    /**
     * Members assigned to this ministry
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ministry_members",
        joinColumns = @JoinColumn(name = "ministry_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<Member> members = new HashSet<>();

    /**
     * Meeting schedule (e.g., "Every Sunday 9:00 AM", "First Saturday of the month")
     */
    @Column(length = 200)
    private String meetingSchedule;

    /**
     * Contact email for the ministry
     */
    @Column(length = 100)
    private String contactEmail;

    /**
     * Contact phone for the ministry
     */
    @Column(length = 20)
    private String contactPhone;

    // Multi-tenancy: Each ministry belongs to a church
    @Column(name = "church_id", nullable = false)
    private Long churchId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
