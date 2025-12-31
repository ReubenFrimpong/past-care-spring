package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.InvitationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for InvitationCode entity.
 */
@Repository
public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Long> {

    /**
     * Find invitation code by code string.
     */
    Optional<InvitationCode> findByCode(String code);

    /**
     * Find all codes for a church.
     */
    List<InvitationCode> findByChurchId(Long churchId);

    /**
     * Find active codes for a church.
     */
    List<InvitationCode> findByChurchIdAndIsActiveTrue(Long churchId);

    /**
     * Find code by code string and church ID.
     */
    Optional<InvitationCode> findByCodeAndChurchId(String code, Long churchId);

    /**
     * Count active codes for a church.
     */
    Long countByChurchIdAndIsActiveTrue(Long churchId);

    /**
     * Find expired codes that haven't been deactivated yet.
     */
    @Query("SELECT i FROM InvitationCode i WHERE i.isActive = true AND i.expiresAt IS NOT NULL AND i.expiresAt < :now")
    List<InvitationCode> findExpiredActiveCodes(@Param("now") LocalDateTime now);

    /**
     * Find codes that have reached their usage limit.
     */
    @Query("SELECT i FROM InvitationCode i WHERE i.isActive = true AND i.maxUses IS NOT NULL AND i.usedCount >= i.maxUses")
    List<InvitationCode> findFullyUsedCodes();

    /**
     * Delete all codes for a church.
     */
    void deleteByChurchId(Long churchId);
}
