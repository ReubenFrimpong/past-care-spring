package com.reuben.pastcare_spring.models;

import com.reuben.pastcare_spring.enums.SkillCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Skill entity representing skills that members can have
 * Used for talent registry, ministry matching, and volunteer coordination
 */
@Entity
@Table(name = "skills", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "church_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SkillCategory category;

    @Column(nullable = false)
    private Boolean isActive = true;

    // Multi-tenancy: Each skill belongs to a church
    @Column(name = "church_id", nullable = false)
    private Long churchId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
