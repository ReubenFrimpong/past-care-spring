package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.PortalUser;
import com.reuben.pastcare_spring.models.PortalUserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortalUserRepository extends JpaRepository<PortalUser, Long> {

    /**
     * Find all portal users by email (across all churches)
     */
    @Query("SELECT p FROM PortalUser p WHERE p.email = :email")
    List<PortalUser> findByEmail(@Param("email") String email);

    /**
     * Find portal user by email and church
     */
    @Query("SELECT p FROM PortalUser p WHERE p.email = :email AND p.church.id = :churchId")
    Optional<PortalUser> findByEmailAndChurchId(@Param("email") String email, @Param("churchId") Long churchId);

    /**
     * Find portal user by member ID
     */
    Optional<PortalUser> findByMemberId(Long memberId);

    /**
     * Find portal user by verification token
     */
    Optional<PortalUser> findByVerificationToken(String token);

    /**
     * Find portal user by password reset token
     */
    Optional<PortalUser> findByPasswordResetToken(String token);

    /**
     * Find all portal users by status for a church
     */
    @Query("SELECT p FROM PortalUser p WHERE p.church.id = :churchId AND p.status = :status")
    List<PortalUser> findByChurchIdAndStatus(@Param("churchId") Long churchId, @Param("status") PortalUserStatus status);

    /**
     * Find all active portal users for a church
     */
    @Query("SELECT p FROM PortalUser p WHERE p.church.id = :churchId AND p.isActive = :isActive")
    List<PortalUser> findByChurchIdAndIsActive(@Param("churchId") Long churchId, @Param("isActive") Boolean isActive);

    /**
     * Count portal users by status for a church
     */
    @Query("SELECT COUNT(p) FROM PortalUser p WHERE p.church.id = :churchId AND p.status = :status")
    Long countByChurchIdAndStatus(@Param("churchId") Long churchId, @Param("status") PortalUserStatus status);

    /**
     * Delete expired verification tokens
     */
    @Query("DELETE FROM PortalUser p WHERE p.verificationTokenExpiry < :now AND p.status = :status")
    void deleteExpiredVerificationTokens(@Param("now") LocalDateTime now, @Param("status") PortalUserStatus status);

    /**
     * Find portal users with expired password reset tokens
     */
    @Query("SELECT p FROM PortalUser p WHERE p.passwordResetTokenExpiry < :now AND p.passwordResetToken IS NOT NULL")
    List<PortalUser> findExpiredPasswordResetTokens(@Param("now") LocalDateTime now);
}
