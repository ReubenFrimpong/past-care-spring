package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.CongregationPricingTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link CongregationPricingTier} entities.
 *
 * @since 2026-01-01
 */
@Repository
public interface CongregationPricingTierRepository extends JpaRepository<CongregationPricingTier, Long> {

    /**
     * Find tier by internal tier name.
     *
     * @param tierName Tier name (e.g., "TIER_1")
     * @return Optional containing tier if found
     */
    Optional<CongregationPricingTier> findByTierName(String tierName);

    /**
     * Find all active tiers ordered by display order.
     *
     * @return List of active tiers
     */
    List<CongregationPricingTier> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find the appropriate pricing tier for a given member count.
     *
     * <p>Returns the tier where:
     * - memberCount >= minMembers
     * - memberCount <= maxMembers (or maxMembers IS NULL for unlimited)
     * - isActive = true
     *
     * @param memberCount Number of members in congregation
     * @return Optional containing matching tier if found
     */
    @Query("""
        SELECT cpt FROM CongregationPricingTier cpt
        WHERE cpt.isActive = true
          AND cpt.minMembers <= :memberCount
          AND (cpt.maxMembers IS NULL OR cpt.maxMembers >= :memberCount)
        ORDER BY cpt.minMembers DESC
        LIMIT 1
        """)
    Optional<CongregationPricingTier> findTierForMemberCount(@Param("memberCount") int memberCount);

    /**
     * Find all tiers with member count in a specific range.
     *
     * @param minMembers Minimum member count
     * @param maxMembers Maximum member count
     * @return List of matching tiers
     */
    @Query("""
        SELECT cpt FROM CongregationPricingTier cpt
        WHERE cpt.isActive = true
          AND (
              (cpt.minMembers BETWEEN :minMembers AND :maxMembers)
              OR (cpt.maxMembers BETWEEN :minMembers AND :maxMembers)
              OR (cpt.minMembers <= :minMembers AND (cpt.maxMembers IS NULL OR cpt.maxMembers >= :maxMembers))
          )
        ORDER BY cpt.displayOrder ASC
        """)
    List<CongregationPricingTier> findTiersInMemberRange(
            @Param("minMembers") int minMembers,
            @Param("maxMembers") int maxMembers
    );

    /**
     * Count number of active tiers.
     *
     * @return Number of active tiers
     */
    long countByIsActiveTrue();

    /**
     * Check if a tier name already exists.
     *
     * @param tierName Tier name to check
     * @return true if exists, false otherwise
     */
    boolean existsByTierName(String tierName);
}
