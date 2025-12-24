package com.reuben.pastcare_spring.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.reuben.pastcare_spring.models.Visitor;

/**
 * Repository for Visitor entity.
 * Phase 1: Enhanced Attendance Tracking
 */
@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {

  /**
   * Find visitor by phone number (unique within tenant).
   */
  Optional<Visitor> findByPhoneNumber(String phoneNumber);

  /**
   * Find visitor by email (unique within tenant).
   */
  Optional<Visitor> findByEmail(String email);

  /**
   * Find all first-time visitors.
   */
  List<Visitor> findByIsFirstTime(Boolean isFirstTime);

  /**
   * Find all visitors who have not been converted to members.
   */
  List<Visitor> findByConvertedToMember(Boolean convertedToMember);

  /**
   * Find visitors by age group.
   */
  List<Visitor> findByAgeGroup(String ageGroup);

  /**
   * Check if visitor exists by phone number.
   */
  boolean existsByPhoneNumber(String phoneNumber);

  /**
   * Check if visitor exists by email.
   */
  boolean existsByEmail(String email);

  // Phase 2: Analytics Queries

  /**
   * Count total visitors in date range
   */
  @Query("SELECT COUNT(DISTINCT v) FROM Visitor v " +
         "WHERE v.lastVisitDate BETWEEN :startDate AND :endDate")
  Long countVisitorsByDateRange(@Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

  /**
   * Count returning visitors (visited more than once)
   */
  @Query("SELECT COUNT(v) FROM Visitor v " +
         "WHERE v.isFirstTime = false " +
         "AND v.lastVisitDate BETWEEN :startDate AND :endDate")
  Long countReturningVisitors(@Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);

  /**
   * Count visitors converted to members
   */
  @Query("SELECT COUNT(v) FROM Visitor v " +
         "WHERE v.convertedToMember = true " +
         "AND v.lastVisitDate BETWEEN :startDate AND :endDate")
  Long countConvertedVisitors(@Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);

  /**
   * Get visitor statistics by age group
   */
  @Query("SELECT v.ageGroup, COUNT(v) FROM Visitor v " +
         "WHERE v.lastVisitDate BETWEEN :startDate AND :endDate " +
         "GROUP BY v.ageGroup")
  List<Object[]> getVisitorsByAgeGroup(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
                                        
}
