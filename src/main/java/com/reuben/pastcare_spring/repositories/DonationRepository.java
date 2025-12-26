package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Campaign;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Donation;
import com.reuben.pastcare_spring.models.DonationType;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.Pledge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Giving Module Phase 1: Donation Recording
 * Repository for donation data access
 */
@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

  /**
   * Find all donations for a church
   */
  List<Donation> findByChurch(Church church);

  /**
   * Find donations by member
   */
  List<Donation> findByMember(Member member);

  /**
   * Find donations by member and church (for multi-tenant safety)
   */
  List<Donation> findByMemberAndChurch(Member member, Church church);

  /**
   * Find donations by date range
   */
  @Query("SELECT d FROM Donation d WHERE d.church = :church AND d.donationDate BETWEEN :startDate AND :endDate ORDER BY d.donationDate DESC")
  List<Donation> findByChurchAndDateRange(
      @Param("church") Church church,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * Find donations by type
   */
  List<Donation> findByChurchAndDonationType(Church church, DonationType donationType);

  /**
   * Find donations by campaign
   */
  List<Donation> findByChurchAndCampaign(Church church, String campaign);

  /**
   * Find anonymous donations
   */
  List<Donation> findByChurchAndIsAnonymousTrue(Church church);

  /**
   * Find donations without receipts
   */
  List<Donation> findByChurchAndReceiptIssuedFalse(Church church);

  /**
   * Get total donations for a church
   */
  @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.church = :church")
  BigDecimal getTotalDonations(@Param("church") Church church);

  /**
   * Get total donations by date range
   */
  @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.church = :church AND d.donationDate BETWEEN :startDate AND :endDate")
  BigDecimal getTotalDonationsByDateRange(
      @Param("church") Church church,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * Get total donations by type
   */
  @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.church = :church AND d.donationType = :donationType")
  BigDecimal getTotalDonationsByType(
      @Param("church") Church church,
      @Param("donationType") DonationType donationType
  );

  /**
   * Get total donations by member
   */
  @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.member = :member AND d.church = :church")
  BigDecimal getTotalDonationsByMember(
      @Param("member") Member member,
      @Param("church") Church church
  );

  /**
   * Get donation count for a church
   */
  long countByChurch(Church church);

  /**
   * Get donation count by date range
   */
  @Query("SELECT COUNT(d) FROM Donation d WHERE d.church = :church AND d.donationDate BETWEEN :startDate AND :endDate")
  long countByChurchAndDateRange(
      @Param("church") Church church,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * Get top donors (members with highest total donations)
   */
  @Query("SELECT d.member, SUM(d.amount) as total FROM Donation d " +
         "WHERE d.church = :church AND d.member IS NOT NULL " +
         "GROUP BY d.member ORDER BY total DESC")
  List<Object[]> getTopDonors(@Param("church") Church church);

  /**
   * Get donations by type breakdown
   */
  @Query("SELECT d.donationType, SUM(d.amount), COUNT(d) FROM Donation d " +
         "WHERE d.church = :church " +
         "GROUP BY d.donationType ORDER BY SUM(d.amount) DESC")
  List<Object[]> getDonationsByTypeBreakdown(@Param("church") Church church);

  /**
   * Get monthly donation totals
   */
  @Query(value = "SELECT DATE_FORMAT(donation_date, '%Y-%m') as month, " +
                 "SUM(amount) as total, COUNT(*) as count " +
                 "FROM donation " +
                 "WHERE church_id = :churchId " +
                 "AND donation_date BETWEEN :startDate AND :endDate " +
                 "GROUP BY DATE_FORMAT(donation_date, '%Y-%m') " +
                 "ORDER BY month DESC",
         nativeQuery = true)
  List<Object[]> getMonthlyDonationTotals(
      @Param("churchId") Long churchId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * Find donations by campaign entity (Phase 3)
   */
  List<Donation> findByCampaignEntityAndChurch(Campaign campaign, Church church);

  /**
   * Find donations by pledge (Phase 3)
   */
  List<Donation> findByPledgeAndChurch(Pledge pledge, Church church);

  /**
   * Count donations by campaign (Phase 3)
   */
  long countByCampaignEntityAndChurch(Campaign campaign, Church church);

  /**
   * Get total donations for a campaign (Phase 3)
   */
  @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.campaignEntity = :campaign AND d.church = :church")
  BigDecimal getTotalDonationsByCampaign(
      @Param("campaign") Campaign campaign,
      @Param("church") Church church
  );
}
