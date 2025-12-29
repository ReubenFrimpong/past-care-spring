package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.StorageUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StorageUsageRepository extends JpaRepository<StorageUsage, Long> {

    /**
     * Get the most recent storage usage for a church.
     */
    Optional<StorageUsage> findFirstByChurchIdOrderByCalculatedAtDesc(Long churchId);

    /**
     * Get all storage usage records for a church within a date range.
     */
    List<StorageUsage> findByChurchIdAndCalculatedAtBetweenOrderByCalculatedAtDesc(
            Long churchId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Get the latest storage usage for all churches.
     */
    @Query("SELECT su FROM StorageUsage su WHERE su.id IN " +
           "(SELECT MAX(su2.id) FROM StorageUsage su2 GROUP BY su2.church.id)")
    List<StorageUsage> findLatestForAllChurches();

    /**
     * Delete old storage usage records (keep only last 90 days).
     */
    void deleteByCalculatedAtBefore(LocalDateTime date);
}
