package com.reuben.pastcare_spring.models;

import com.reuben.pastcare_spring.enums.ProficiencyLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * MemberSkill entity representing the many-to-many relationship between members and skills
 * with additional attributes like proficiency level and availability
 */
@Entity
@Table(name = "member_skills", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"member_id", "skill_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProficiencyLevel proficiencyLevel;

    @Column(nullable = false)
    private Boolean willingToServe = true;

    @Column(nullable = false)
    private Boolean currentlyServing = false;

    /**
     * Years of experience with this skill
     */
    @Column
    private Integer yearsOfExperience;

    /**
     * Notes about the skill (e.g., specific instruments, certifications, availability)
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Date when the member acquired this skill
     */
    @Column
    private LocalDateTime acquiredDate;

    /**
     * Date when the member was last verified/assessed for this skill
     */
    @Column
    private LocalDateTime lastVerifiedDate;

    // Multi-tenancy: Inherited from member, but stored for quick filtering
    @Column(name = "church_id", nullable = false)
    private Long churchId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
