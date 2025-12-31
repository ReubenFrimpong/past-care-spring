package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.ChurchStorageAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ChurchStorageAddon entity.
 *
 * <p>Provides queries for:
 * <ul>
 *   <li>Finding active addons by church</li>
 *   <li>Calculating total addon storage</li>
 *   <li>Preventing duplicate addon purchases</li>
 *   <li>Finding addons due for renewal</li>
 *   <li>Status-based queries</li>
 * </ul>
 */
@Repository
public interface ChurchStorageAddonRepository extends JpaRepository<ChurchStorageAddon, Long> {

    /**
     * Find all addons for a church by status
     *
     * @param churchId Church ID
     * @param status Status (ACTIVE, CANCELED, SUSPENDED)
     * @return List of addons with matching status
     */
    List<ChurchStorageAddon> findByChurchIdAndStatus(Long churchId, String status);

    /**
     * Find all addons for a church (all statuses)
     *
     * @param churchId Church ID
     * @return List of all addons for church
     */
    List<ChurchStorageAddon> findByChurchId(Long churchId);

    /**
     * Check if church already purchased a specific addon with given status
     *
     * @param churchId Church ID
     * @param storageAddonId Storage addon ID
     * @param status Status to check
     * @return true if addon exists with status
     */
    boolean existsByChurchIdAndStorageAddonIdAndStatus(
            Long churchId,
            Long storageAddonId,
            String status
    );

    /**
     * Find specific addon by church and addon ID with status
     *
     * @param churchId Church ID
     * @param storageAddonId Storage addon ID
     * @param status Status
     * @return Optional addon
     */
    Optional<ChurchStorageAddon> findByChurchIdAndStorageAddonIdAndStatus(
            Long churchId,
            Long storageAddonId,
            String status
    );

    /**
     * Find addon by purchase reference (for webhook verification)
     *
     * @param reference Purchase reference (ADDON-{UUID})
     * @return Optional addon
     */
    Optional<ChurchStorageAddon> findByPurchaseReference(String reference);

    /**
     * Calculate total addon storage in GB for a church (only ACTIVE addons)
     *
     * @param churchId Church ID
     * @return Total addon storage in GB, or 0 if no addons
     */
    @Query("SELECT COALESCE(SUM(sa.storageGb), 0) " +
           "FROM ChurchStorageAddon csa " +
           "JOIN csa.storageAddon sa " +
           "WHERE csa.churchId = :churchId AND csa.status = 'ACTIVE'")
    Long sumActiveStorageGbByChurchId(@Param("churchId") Long churchId);

    /**
     * Calculate total addon storage in MB for a church (only ACTIVE addons)
     *
     * @param churchId Church ID
     * @return Total addon storage in MB, or 0 if no addons
     */
    @Query("SELECT COALESCE(SUM(sa.storageGb * 1024), 0) " +
           "FROM ChurchStorageAddon csa " +
           "JOIN csa.storageAddon sa " +
           "WHERE csa.churchId = :churchId AND csa.status = 'ACTIVE'")
    Long sumActiveStorageMbByChurchId(@Param("churchId") Long churchId);

    /**
     * Find all active addons due for renewal on a specific date
     *
     * @param renewalDate Date to check
     * @return List of addons due for renewal
     */
    @Query("SELECT csa FROM ChurchStorageAddon csa " +
           "WHERE csa.status = 'ACTIVE' " +
           "AND csa.nextRenewalDate = :renewalDate")
    List<ChurchStorageAddon> findActiveDueForRenewal(@Param("renewalDate") LocalDate renewalDate);

    /**
     * Find all active addons for a church with eager-loaded addon details
     *
     * @param churchId Church ID
     * @return List of active addons with StorageAddon eagerly loaded
     */
    @Query("SELECT csa FROM ChurchStorageAddon csa " +
           "JOIN FETCH csa.storageAddon " +
           "WHERE csa.churchId = :churchId AND csa.status = 'ACTIVE'")
    List<ChurchStorageAddon> findActiveAddonsWithDetails(@Param("churchId") Long churchId);

    /**
     * Find all suspended addons for a church
     *
     * @param churchId Church ID
     * @return List of suspended addons
     */
    @Query("SELECT csa FROM ChurchStorageAddon csa " +
           "WHERE csa.churchId = :churchId AND csa.status = 'SUSPENDED'")
    List<ChurchStorageAddon> findSuspendedByChurchId(@Param("churchId") Long churchId);

    /**
     * Count active addons for a church
     *
     * @param churchId Church ID
     * @return Number of active addons
     */
    @Query("SELECT COUNT(csa) FROM ChurchStorageAddon csa " +
           "WHERE csa.churchId = :churchId AND csa.status = 'ACTIVE'")
    Long countActiveByChurchId(@Param("churchId") Long churchId);

    /**
     * Find all canceled addons that are past their period end (grace period expired)
     *
     * @param currentDate Current date
     * @return List of expired canceled addons
     */
    @Query("SELECT csa FROM ChurchStorageAddon csa " +
           "WHERE csa.status = 'CANCELED' " +
           "AND csa.currentPeriodEnd < :currentDate")
    List<ChurchStorageAddon> findExpiredCanceled(@Param("currentDate") LocalDate currentDate);
}
