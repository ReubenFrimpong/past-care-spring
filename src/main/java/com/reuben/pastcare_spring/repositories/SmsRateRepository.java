package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.SmsRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SmsRateRepository extends JpaRepository<SmsRate, Long> {

    Optional<SmsRate> findByCountryCodeAndIsActive(String countryCode, Boolean isActive);

    @Query("SELECT r FROM SmsRate r WHERE r.countryCode = 'OTHER' AND r.isActive = true")
    Optional<SmsRate> findDefaultRate();

    List<SmsRate> findByIsLocalAndIsActive(Boolean isLocal, Boolean isActive);

    List<SmsRate> findByIsActive(Boolean isActive);

    @Query("SELECT r FROM SmsRate r WHERE (r.church.id = :churchId OR r.church IS NULL) " +
           "AND r.countryCode = :countryCode AND r.isActive = true ORDER BY r.church.id DESC LIMIT 1")
    Optional<SmsRate> findRateForChurchAndCountry(
        @Param("churchId") Long churchId,
        @Param("countryCode") String countryCode);
}
