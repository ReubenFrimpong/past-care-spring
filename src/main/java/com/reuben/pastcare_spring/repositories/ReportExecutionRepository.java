package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.ReportExecution;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ReportExecution entity operations.
 */
@Repository
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, Long> {

    List<ReportExecution> findByReportIdOrderByExecutionDateDesc(Long reportId);

    List<ReportExecution> findByChurchIdOrderByExecutionDateDesc(Long churchId, Pageable pageable);

    List<ReportExecution> findByExecutedByIdOrderByExecutionDateDesc(Long userId);

    Optional<ReportExecution> findByIdAndChurchId(Long id, Long churchId);
}
