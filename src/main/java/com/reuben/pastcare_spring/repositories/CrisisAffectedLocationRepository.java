package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Crisis;
import com.reuben.pastcare_spring.models.CrisisAffectedLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrisisAffectedLocationRepository extends JpaRepository<CrisisAffectedLocation, Long> {

    // Find all locations for a crisis
    List<CrisisAffectedLocation> findByCrisis(Crisis crisis);

    // Find by crisis ID
    List<CrisisAffectedLocation> findByCrisisId(Long crisisId);

    // Delete all locations for a crisis
    void deleteByCrisis(Crisis crisis);

    // Check if a specific location exists for a crisis
    @Query("SELECT COUNT(cal) > 0 FROM CrisisAffectedLocation cal WHERE cal.crisis = :crisis " +
           "AND (:suburb IS NULL OR LOWER(cal.suburb) = LOWER(:suburb)) " +
           "AND (:city IS NULL OR LOWER(cal.city) = LOWER(:city)) " +
           "AND (:district IS NULL OR LOWER(cal.district) = LOWER(:district)) " +
           "AND (:region IS NULL OR LOWER(cal.region) = LOWER(:region)) " +
           "AND (:countryCode IS NULL OR cal.countryCode = :countryCode)")
    boolean existsByLocation(
        @Param("crisis") Crisis crisis,
        @Param("suburb") String suburb,
        @Param("city") String city,
        @Param("district") String district,
        @Param("region") String region,
        @Param("countryCode") String countryCode
    );
}
