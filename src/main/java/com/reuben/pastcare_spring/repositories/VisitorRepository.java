package com.reuben.pastcare_spring.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
