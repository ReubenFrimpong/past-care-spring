package com.reuben.pastcare_spring.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Member;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {

  // Pagination and search
  Page<Member> findByChurch(Church church, Pageable pageable);

  @Query("SELECT m FROM Member m LEFT JOIN m.location l WHERE m.church = :church AND " +
         "(LOWER(m.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(m.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(m.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(l.city) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(l.suburb) LIKE LOWER(CONCAT('%', :search, '%')))")
  Page<Member> searchMembers(@Param("church") Church church, @Param("search") String search, Pageable pageable);

  // Filters
  Page<Member> findByChurchAndIsVerified(Church church, Boolean isVerified, Pageable pageable);

  Page<Member> findByChurchAndMaritalStatus(Church church, String maritalStatus, Pageable pageable);

  @Query("SELECT m FROM Member m LEFT JOIN m.location l WHERE m.church = :church AND " +
         "(LOWER(m.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(m.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(m.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(l.city) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(l.suburb) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
         "m.isVerified = :isVerified")
  Page<Member> searchMembersWithVerifiedFilter(@Param("church") Church church, @Param("search") String search,
                                                 @Param("isVerified") Boolean isVerified, Pageable pageable);

  @Query("SELECT m FROM Member m LEFT JOIN m.location l WHERE m.church = :church AND " +
         "(LOWER(m.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(m.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(m.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(l.city) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(l.suburb) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
         "m.maritalStatus = :maritalStatus")
  Page<Member> searchMembersWithMaritalFilter(@Param("church") Church church, @Param("search") String search,
                                                @Param("maritalStatus") String maritalStatus, Pageable pageable);

  // Statistics
  long countByChurch(Church church);

  long countByChurchAndIsVerified(Church church, Boolean isVerified);

  long countByChurchAndMemberSinceAfter(Church church, YearMonth startDate);

  // Location-based statistics for map visualization
  @Query("SELECT new com.reuben.pastcare_spring.dtos.LocationStatsResponse(" +
         "l.id, l.city, l.coordinates, l.city, l.suburb, l.region, COUNT(m.id)) " +
         "FROM Member m JOIN m.location l " +
         "WHERE m.church = :church AND m.location IS NOT NULL " +
         "GROUP BY l.id, l.city, l.coordinates, l.suburb, l.region " +
         "ORDER BY COUNT(m.id) DESC")
  java.util.List<com.reuben.pastcare_spring.dtos.LocationStatsResponse> getLocationStatistics(@Param("church") Church church);

  // Phase 2: Quick Operations
  /**
   * Find member by phone number (for duplicate detection in quick add).
   * @param phoneNumber The phone number to search for
   * @return Optional containing the member if found
   */
  Optional<Member> findByPhoneNumber(String phoneNumber);

  // Phase 2.7: Tags
  /**
   * Find all members in a church that have a specific tag.
   * @param church The church to search in
   * @param tag The tag to search for
   * @param pageable Pagination information
   * @return Page of members with the tag
   */
  @Query("SELECT m FROM Member m JOIN m.tags t WHERE m.church = :church AND t = :tag")
  Page<Member> findByChurchAndTagsContaining(@Param("church") Church church, @Param("tag") String tag, Pageable pageable);

  // Phase 3: Household Management
  /**
   * Find member by ID and church (for tenant isolation).
   * @param id The member ID
   * @param church The church
   * @return Optional containing the member if found
   */
  Optional<Member> findByIdAndChurch(Long id, Church church);

  /**
   * Count members in a church that belong to a household.
   * @param church The church
   * @return Count of members with a household
   */
  long countByChurchAndHouseholdIsNotNull(Church church);

  // Phase 4: Export and Integration Queries
  /**
   * Find all members by church ID.
   * @param churchId The church ID
   * @return List of members in the church
   */
  @Query("SELECT m FROM Member m WHERE m.church.id = :churchId")
  java.util.List<Member> findByChurchId(@Param("churchId") Long churchId);

  // Dashboard Phase 1: Enhanced Widgets

  /**
   * Find members with birthdays this week.
   * Returns members whose birthday falls within the current week.
   */
  @Query(value = "SELECT m.id as memberId, m.first_name as firstName, m.last_name as lastName, " +
                 "m.dob as dateOfBirth, " +
                 "TIMESTAMPDIFF(YEAR, m.dob, CURDATE()) as age, " +
                 "CASE " +
                 "  WHEN DAYOFYEAR(DATE_ADD(CURDATE(), INTERVAL (YEAR(CURDATE()) - YEAR(m.dob)) YEAR)) = DAYOFYEAR(CURDATE()) THEN 'Today' " +
                 "  WHEN DAYOFYEAR(DATE_ADD(CURDATE(), INTERVAL (YEAR(CURDATE()) - YEAR(m.dob)) YEAR)) = DAYOFYEAR(CURDATE()) + 1 THEN 'Tomorrow' " +
                 "  ELSE CONCAT('In ', DATEDIFF(DATE_ADD(m.dob, INTERVAL (YEAR(CURDATE()) - YEAR(m.dob)) YEAR), CURDATE()), ' days') " +
                 "END as daysUntil " +
                 "FROM member m " +
                 "WHERE m.church_id = :#{#church.id} " +
                 "AND m.dob IS NOT NULL " +
                 "AND WEEK(DATE_ADD(m.dob, INTERVAL (YEAR(CURDATE()) - YEAR(m.dob)) YEAR)) = WEEK(CURDATE()) " +
                 "ORDER BY DAYOFYEAR(DATE_ADD(m.dob, INTERVAL (YEAR(CURDATE()) - YEAR(m.dob)) YEAR))",
         nativeQuery = true)
  java.util.List<com.reuben.pastcare_spring.dtos.BirthdayResponse> findMembersWithBirthdaysThisWeek(@Param("church") Church church);

  /**
   * Find members with membership anniversaries this month.
   * Returns members whose memberSince anniversary falls in the current month.
   */
  @Query(value = "SELECT m.id as memberId, m.first_name as firstName, m.last_name as lastName, " +
                 "m.member_since as memberSince, " +
                 "TIMESTAMPDIFF(YEAR, CONCAT(m.member_since, '-01'), CURDATE()) as yearsOfMembership " +
                 "FROM member m " +
                 "WHERE m.church_id = :#{#church.id} " +
                 "AND m.member_since IS NOT NULL " +
                 "AND MONTH(CONCAT(m.member_since, '-01')) = MONTH(CURDATE()) " +
                 "ORDER BY DAY(CONCAT(m.member_since, '-01'))",
         nativeQuery = true)
  java.util.List<com.reuben.pastcare_spring.dtos.AnniversaryResponse> findMembersWithAnniversariesThisMonth(@Param("church") Church church);

  /**
   * Find irregular attenders (members who haven't attended in N weeks).
   * Uses attendance data to identify members absent for threshold weeks.
   */
  @Query(value = "SELECT m.id as memberId, m.first_name as firstName, m.last_name as lastName, " +
                 "m.phone_number as phoneNumber, " +
                 "MAX(a.created_at) as lastAttendanceDate, " +
                 "TIMESTAMPDIFF(WEEK, MAX(a.created_at), CURDATE()) as weeksAbsent " +
                 "FROM member m " +
                 "LEFT JOIN attendance a ON m.id = a.member_id " +
                 "WHERE m.church_id = :#{#church.id} " +
                 "GROUP BY m.id " +
                 "HAVING weeksAbsent >= :weeksThreshold OR lastAttendanceDate IS NULL " +
                 "ORDER BY weeksAbsent DESC " +
                 "LIMIT 10",
         nativeQuery = true)
  java.util.List<com.reuben.pastcare_spring.dtos.IrregularAttenderResponse> findIrregularAttenders(
      @Param("church") Church church,
      @Param("weeksThreshold") int weeksThreshold);

  /**
   * Get member growth trend for the last N months.
   * Returns monthly new member count and total member count.
   */
  @Query(value = "SELECT DATE_FORMAT(MIN(m.created_at), '%b %Y') as month, " +
                 "CAST(COUNT(*) AS SIGNED) as newMembers, " +
                 "CAST((SELECT COUNT(*) FROM member WHERE church_id = :#{#church.id} AND created_at <= LAST_DAY(MIN(m.created_at))) AS SIGNED) as totalMembers " +
                 "FROM member m " +
                 "WHERE m.church_id = :#{#church.id} " +
                 "AND m.created_at >= DATE_SUB(CURDATE(), INTERVAL :months MONTH) " +
                 "GROUP BY YEAR(m.created_at), MONTH(m.created_at) " +
                 "ORDER BY YEAR(MIN(m.created_at)) DESC, MONTH(MIN(m.created_at)) DESC",
         nativeQuery = true)
  java.util.List<com.reuben.pastcare_spring.dtos.MemberGrowthResponse> getMemberGrowthTrend(
      @Param("church") Church church,
      @Param("months") int months);

}
