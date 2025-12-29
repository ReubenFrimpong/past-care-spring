package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Report entity operations.
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByChurchIdAndIsCustom(Long churchId, Boolean isCustom);

    List<Report> findByChurchIdAndIsTemplate(Long churchId, Boolean isTemplate);

    List<Report> findByChurchIdAndCreatedById(Long churchId, Long userId);

    List<Report> findByChurchIdAndIsSharedTrue(Long churchId);

    Optional<Report> findByIdAndChurchId(Long id, Long churchId);

    List<Report> findByChurchId(Long churchId);
}
