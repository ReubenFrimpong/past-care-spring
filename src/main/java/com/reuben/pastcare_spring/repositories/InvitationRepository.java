package com.reuben.pastcare_spring.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Invitation;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

  /**
   * Find an invitation by token.
   */
  Optional<Invitation> findByToken(String token);

  /**
   * Find all invitations for a church.
   */
  List<Invitation> findByChurch(Church church);

  /**
   * Find all invitations sent to a specific email.
   */
  List<Invitation> findByEmail(String email);

  /**
   * Find all valid (unused and not expired) invitations for a church.
   */
  List<Invitation> findByChurchAndUsedFalseAndExpiresAtAfter(Church church, LocalDateTime now);

  /**
   * Find all pending invitations sent to a specific email.
   */
  List<Invitation> findByEmailAndUsedFalseAndExpiresAtAfter(String email, LocalDateTime now);

  /**
   * Check if there's a valid invitation for the given email and church.
   */
  boolean existsByEmailAndChurchAndUsedFalseAndExpiresAtAfter(
      String email, Church church, LocalDateTime now);

  /**
   * Delete all expired invitations (cleanup task).
   */
  void deleteByExpiresAtBefore(LocalDateTime cutoffDate);
}
