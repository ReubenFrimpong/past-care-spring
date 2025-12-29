package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Campaign;
import com.reuben.pastcare_spring.models.CampaignStatus;
import com.reuben.pastcare_spring.models.Church;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * Repository for campaign data access
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    java.util.List<Campaign> findByChurch_Id(Long churchId);

  /**
   * Find all campaigns for a church
   */
  List<Campaign> findByChurch(Church church);

  /**
   * Find campaign by ID and church (multi-tenant safety)
   */
  Optional<Campaign> findByIdAndChurch(Long id, Church church);

  /**
   * Find campaigns by status
   */
  List<Campaign> findByChurchAndStatus(Church church, CampaignStatus status);

  /**
   * Find active campaigns
   */
  @Query("SELECT c FROM Campaign c WHERE c.church = :church AND c.status = 'ACTIVE' ORDER BY c.startDate DESC")
  List<Campaign> findActiveCampaigns(@Param("church") Church church);

  /**
   * Find featured campaigns
   */
  @Query("SELECT c FROM Campaign c WHERE c.church = :church AND c.featured = true AND c.status = 'ACTIVE' ORDER BY c.startDate DESC")
  List<Campaign> findFeaturedCampaigns(@Param("church") Church church);

  /**
   * Find public campaigns (visible in member portal)
   */
  @Query("SELECT c FROM Campaign c WHERE c.church = :church AND c.isPublic = true AND c.status = 'ACTIVE' ORDER BY c.startDate DESC")
  List<Campaign> findPublicCampaigns(@Param("church") Church church);

  /**
   * Find campaigns within date range
   */
  @Query("SELECT c FROM Campaign c WHERE c.church = :church AND c.startDate <= :endDate AND (c.endDate IS NULL OR c.endDate >= :startDate) ORDER BY c.startDate DESC")
  List<Campaign> findCampaignsInDateRange(
      @Param("church") Church church,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * Find ongoing campaigns (no end date or end date in future)
   */
  @Query("SELECT c FROM Campaign c WHERE c.church = :church AND c.status = 'ACTIVE' AND (c.endDate IS NULL OR c.endDate >= :currentDate) ORDER BY c.startDate DESC")
  List<Campaign> findOngoingCampaigns(
      @Param("church") Church church,
      @Param("currentDate") LocalDate currentDate
  );

  /**
   * Find campaigns that have reached their goal
   */
  @Query("SELECT c FROM Campaign c WHERE c.church = :church AND c.currentAmount >= c.goalAmount ORDER BY c.startDate DESC")
  List<Campaign> findGoalReachedCampaigns(@Param("church") Church church);

  /**
   * Find campaigns by name (partial match)
   */
  @Query("SELECT c FROM Campaign c WHERE c.church = :church AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY c.startDate DESC")
  List<Campaign> findByChurchAndNameContaining(
      @Param("church") Church church,
      @Param("name") String name
  );

  /**
   * Count campaigns by status
   */
  long countByChurchAndStatus(Church church, CampaignStatus status);

  /**
   * Count active campaigns
   */
  @Query("SELECT COUNT(c) FROM Campaign c WHERE c.church = :church AND c.status = 'ACTIVE'")
  long countActiveCampaigns(@Param("church") Church church);

  /**
   * Count campaigns by church ID
   */
  Long countByChurch_Id(Long churchId);
}
