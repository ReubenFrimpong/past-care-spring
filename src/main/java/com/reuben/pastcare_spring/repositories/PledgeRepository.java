package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Campaign;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.Pledge;
import com.reuben.pastcare_spring.models.PledgeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * Repository for pledge data access
 */
@Repository
public interface PledgeRepository extends JpaRepository<Pledge, Long> {

  /**
   * Find all pledges for a church
   */
  List<Pledge> findByChurch(Church church);

  /**
   * Find pledge by ID and church (multi-tenant safety)
   */
  Optional<Pledge> findByIdAndChurch(Long id, Church church);

  /**
   * Find pledges by member
   */
  List<Pledge> findByMember(Member member);

  /**
   * Find pledges by member and church
   */
  List<Pledge> findByMemberAndChurch(Member member, Church church);

  /**
   * Find pledges by campaign
   */
  List<Pledge> findByCampaign(Campaign campaign);

  /**
   * Find pledges by campaign and church
   */
  List<Pledge> findByCampaignAndChurch(Campaign campaign, Church church);

  /**
   * Find pledges by status
   */
  List<Pledge> findByChurchAndStatus(Church church, PledgeStatus status);

  /**
   * Find active pledges
   */
  @Query("SELECT p FROM Pledge p WHERE p.church = :church AND p.status = 'ACTIVE' ORDER BY p.nextPaymentDate ASC")
  List<Pledge> findActivePledges(@Param("church") Church church);

  /**
   * Find active pledges by member
   */
  @Query("SELECT p FROM Pledge p WHERE p.member = :member AND p.church = :church AND p.status = 'ACTIVE' ORDER BY p.nextPaymentDate ASC")
  List<Pledge> findActivePledgesByMember(
      @Param("member") Member member,
      @Param("church") Church church
  );

  /**
   * Find pledges with upcoming payments (within X days)
   */
  @Query("SELECT p FROM Pledge p WHERE p.church = :church AND p.status = 'ACTIVE' AND p.nextPaymentDate BETWEEN :startDate AND :endDate ORDER BY p.nextPaymentDate ASC")
  List<Pledge> findPledgesWithUpcomingPayments(
      @Param("church") Church church,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * Find overdue pledges
   */
  @Query("SELECT p FROM Pledge p WHERE p.church = :church AND p.status = 'ACTIVE' AND p.nextPaymentDate < :currentDate ORDER BY p.nextPaymentDate ASC")
  List<Pledge> findOverduePledges(
      @Param("church") Church church,
      @Param("currentDate") LocalDate currentDate
  );

  /**
   * Find pledges by member for a specific campaign
   */
  @Query("SELECT p FROM Pledge p WHERE p.member = :member AND p.campaign = :campaign AND p.church = :church")
  List<Pledge> findByMemberAndCampaign(
      @Param("member") Member member,
      @Param("campaign") Campaign campaign,
      @Param("church") Church church
  );

  /**
   * Find completed pledges
   */
  @Query("SELECT p FROM Pledge p WHERE p.church = :church AND p.status = 'COMPLETED' ORDER BY p.pledgeDate DESC")
  List<Pledge> findCompletedPledges(@Param("church") Church church);

  /**
   * Calculate total pledge amount for a campaign
   */
  @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Pledge p WHERE p.campaign = :campaign AND p.church = :church AND p.status != 'CANCELLED'")
  BigDecimal calculateTotalPledgeAmount(
      @Param("campaign") Campaign campaign,
      @Param("church") Church church
  );

  /**
   * Calculate total amount paid for a campaign
   */
  @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Pledge p WHERE p.campaign = :campaign AND p.church = :church AND p.status != 'CANCELLED'")
  BigDecimal calculateTotalPaidAmount(
      @Param("campaign") Campaign campaign,
      @Param("church") Church church
  );

  /**
   * Count active pledges
   */
  @Query("SELECT COUNT(p) FROM Pledge p WHERE p.church = :church AND p.status = 'ACTIVE'")
  long countActivePledges(@Param("church") Church church);

  /**
   * Count pledges by member
   */
  long countByMemberAndChurch(Member member, Church church);

  /**
   * Count pledges by campaign
   */
  long countByCampaignAndChurch(Campaign campaign, Church church);

  /**
   * Find pledges needing reminder (next payment date within reminder window and sendReminders = true)
   */
  @Query("SELECT p FROM Pledge p WHERE p.church = :church AND p.status = 'ACTIVE' AND p.sendReminders = true AND p.nextPaymentDate BETWEEN :startDate AND :endDate ORDER BY p.nextPaymentDate ASC")
  List<Pledge> findPledgesNeedingReminder(
      @Param("church") Church church,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );
}
