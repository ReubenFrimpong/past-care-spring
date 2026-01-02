package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.PartnershipCodeUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnershipCodeUsageRepository extends JpaRepository<PartnershipCodeUsage, Long> {

    /**
     * Check if a church has already used a specific partnership code
     */
    Optional<PartnershipCodeUsage> findByPartnershipCodeIdAndChurchId(Long partnershipCodeId, Long churchId);

    /**
     * Count how many times a church has used a specific partnership code
     */
    long countByPartnershipCodeIdAndChurchId(Long partnershipCodeId, Long churchId);

    /**
     * Count total unique churches that have used a specific partnership code
     */
    @Query("SELECT COUNT(DISTINCT pcu.churchId) FROM PartnershipCodeUsage pcu WHERE pcu.partnershipCodeId = :partnershipCodeId")
    long countUniqueChurchesByPartnershipCodeId(Long partnershipCodeId);

    /**
     * Count total usage records for a specific partnership code
     */
    long countByPartnershipCodeId(Long partnershipCodeId);
}
