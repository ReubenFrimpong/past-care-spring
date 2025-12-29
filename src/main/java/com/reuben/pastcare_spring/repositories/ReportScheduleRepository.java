package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.ReportSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ReportSchedule entity operations.
 */
@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {

    List<ReportSchedule> findByChurchIdAndIsActiveTrue(Long churchId);

    List<ReportSchedule> findByNextExecutionDateBeforeAndIsActiveTrue(LocalDateTime dateTime);

    Optional<ReportSchedule> findByIdAndChurchId(Long id, Long churchId);

    List<ReportSchedule> findByReportId(Long reportId);
}
