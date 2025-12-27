package com.reuben.pastcare_spring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a tag for categorizing events.
 * Supports flexible categorization and filtering of events.
 */
@Entity
@Table(name = "event_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tenant isolation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    @JsonIgnore
    private Church church;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;

    // Tag information
    @Column(name = "tag", nullable = false, length = 100)
    private String tag;

    @Column(name = "tag_color", length = 20)
    private String tagColor;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    @JsonIgnore
    private User createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Business logic methods

    /**
     * Normalize tag name (lowercase, trimmed)
     */
    public void normalizeTag() {
        if (tag != null) {
            this.tag = tag.trim().toLowerCase();
        }
    }

    /**
     * Set default color if not provided
     */
    public void setDefaultColorIfNeeded() {
        if (tagColor == null || tagColor.isEmpty()) {
            this.tagColor = "#3B82F6"; // Default blue color
        }
    }
}
