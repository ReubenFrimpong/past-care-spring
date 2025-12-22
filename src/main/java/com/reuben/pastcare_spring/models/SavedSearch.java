package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a saved search query for members.
 * Allows users to save complex search criteria for reuse.
 * Supports both private (user-specific) and public (church-wide) searches.
 */
@Entity
@Table(name = "saved_searches", indexes = {
    @Index(name = "idx_saved_search_church_user", columnList = "church_id, created_by_user_id"),
    @Index(name = "idx_saved_search_name", columnList = "search_name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 200)
    private String searchName;

    /**
     * JSON string containing the search criteria.
     * Stores the AdvancedSearchRequest in JSON format.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String searchCriteria;

    /**
     * Whether this search is visible to all users in the church.
     * If false, only the creator can see and use this search.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * Whether this search auto-updates as members change.
     * Dynamic searches recalculate results each time they're executed.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDynamic = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Last time this search was executed.
     */
    private LocalDateTime lastExecuted;

    /**
     * Number of results from the last execution.
     */
    private Long lastResultCount;

    @Column(nullable = false, updatable = false)
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
}
