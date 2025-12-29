package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Household;
import com.reuben.pastcare_spring.models.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {
    java.util.List<Household> findByChurch_Id(Long churchId);

    /**
     * Find all households for a specific church
     */
    Page<Household> findByChurch(Church church, Pageable pageable);

    /**
     * Find households by church and name containing (case-insensitive search)
     */
    Page<Household> findByChurchAndHouseholdNameContainingIgnoreCase(
        Church church,
        String name,
        Pageable pageable
    );

    /**
     * Count households in a church
     */
    long countByChurch(Church church);

    /**
     * Find household by ID and church (for tenant isolation)
     */
    Optional<Household> findByIdAndChurch(Long id, Church church);

    /**
     * Find household containing a specific member
     */
    @Query("SELECT h FROM Household h JOIN h.members m WHERE h.church = :church AND m = :member")
    Optional<Household> findByChurchAndMember(
        @Param("church") Church church,
        @Param("member") Member member
    );

    /**
     * Check if household name exists in church (for uniqueness validation)
     */
    boolean existsByChurchAndHouseholdNameIgnoreCase(Church church, String householdName);

    /**
     * Find households by location
     */
    @Query("SELECT h FROM Household h WHERE h.church = :church AND h.sharedLocation.id = :locationId")
    Page<Household> findByChurchAndLocationId(
        @Param("church") Church church,
        @Param("locationId") Long locationId,
        Pageable pageable
    );

    /**
     * Find households with member count greater than or equal to specified value
     */
    @Query("SELECT h FROM Household h WHERE h.church = :church AND SIZE(h.members) >= :minMembers")
    Page<Household> findByChurchAndMemberCountGreaterThanEqual(
        @Param("church") Church church,
        @Param("minMembers") int minMembers,
        Pageable pageable
    );

    /**
     * Delete household by ID and church (for tenant isolation)
     */
    void deleteByIdAndChurch(Long id, Church church);
}
