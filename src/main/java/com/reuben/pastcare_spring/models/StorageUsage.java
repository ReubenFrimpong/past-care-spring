package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to track storage usage for each church (tenant).
 * Used for billing and subscription management.
 *
 * Storage includes:
 * - File storage: profile photos, event images, documents, attachments
 * - Database storage: all table rows (members, donations, events, etc.)
 */
@Entity
@Table(name = "storage_usage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;

    /**
     * File storage in megabytes (MB).
     * Includes: profile photos, event images, document uploads, attachments.
     */
    @Column(name = "file_storage_mb", nullable = false)
    private Double fileStorageMb;

    /**
     * Database storage in megabytes (MB).
     * Estimated based on row counts and average row sizes.
     */
    @Column(name = "database_storage_mb", nullable = false)
    private Double databaseStorageMb;

    /**
     * Total storage in megabytes (MB).
     * fileStorageMb + databaseStorageMb
     */
    @Column(name = "total_storage_mb", nullable = false)
    private Double totalStorageMb;

    /**
     * Breakdown of file storage by category (JSON).
     * Example: {"profilePhotos": 50.5, "eventImages": 120.3, "documents": 30.2}
     */
    @Column(name = "file_storage_breakdown", columnDefinition = "TEXT")
    private String fileStorageBreakdown;

    /**
     * Breakdown of database storage by entity (JSON).
     * Example: {"members": 5.2, "donations": 8.5, "events": 3.1}
     */
    @Column(name = "database_storage_breakdown", columnDefinition = "TEXT")
    private String databaseStorageBreakdown;

    /**
     * When this storage usage snapshot was calculated.
     */
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    /**
     * Timestamp when this record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
    }
}
