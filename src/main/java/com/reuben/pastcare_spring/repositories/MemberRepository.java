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

}
