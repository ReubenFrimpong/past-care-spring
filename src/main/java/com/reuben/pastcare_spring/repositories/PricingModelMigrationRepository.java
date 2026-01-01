package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.PricingModelMigration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link PricingModelMigration} entities.
 *
 * @since 2026-01-01
 */
@Repository
public interface PricingModelMigrationRepository extends JpaRepository<PricingModelMigration, Long> {

    /**
     * Find migration record for a specific church.
     *
     * @param churchId Church ID
     * @return Optional containing migration record if found
     */
    Optional<PricingModelMigration> findByChurchId(Long churchId);

    /**
     * Find all migrations for a specific church.
     *
     * @param churchId Church ID
     * @return List of migration records
     */
    List<PricingModelMigration> findAllByChurchId(Long churchId);

    /**
     * Find latest migration record for a church.
     *
     * @param churchId Church ID
     * @return Optional containing most recent migration
     */
    @Query("""
        SELECT pmm FROM PricingModelMigration pmm
        WHERE pmm.churchId = :churchId
        ORDER BY pmm.migratedAt DESC
        LIMIT 1
        """)
    Optional<PricingModelMigration> findLatestByChurchId(@Param("churchId") Long churchId);

    /**
     * Find all migrations with a specific status.
     *
     * @param status Migration status
     * @return List of migrations
     */
    List<PricingModelMigration> findByMigrationStatus(PricingModelMigration.MigrationStatus status);

    /**
     * Count migrations by status.
     *
     * @param status Migration status
     * @return Count of migrations
     */
    long countByMigrationStatus(PricingModelMigration.MigrationStatus status);

    /**
     * Find all completed migrations.
     *
     * @return List of completed migrations
     */
    @Query("""
        SELECT pmm FROM PricingModelMigration pmm
        WHERE pmm.migrationStatus = 'COMPLETED'
        ORDER BY pmm.migratedAt DESC
        """)
    List<PricingModelMigration> findAllCompleted();

    /**
     * Check if church has been migrated.
     *
     * @param churchId Church ID
     * @return true if migration exists, false otherwise
     */
    boolean existsByChurchId(Long churchId);

    /**
     * Count migrations performed by a specific admin.
     *
     * @param migratedBy Admin user ID
     * @return Count of migrations
     */
    long countByMigratedBy(Long migratedBy);
}
