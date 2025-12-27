package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.CareNeed;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.Visit;
import com.reuben.pastcare_spring.models.VisitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    // Find by church
    List<Visit> findByChurchOrderByVisitDateDesc(Church church);

    // Find by member
    List<Visit> findByMemberOrderByVisitDateDesc(Member member);

    // Find by care need
    List<Visit> findByCareNeedOrderByVisitDateDesc(CareNeed careNeed);

    // Find by type
    List<Visit> findByTypeOrderByVisitDateDesc(VisitType type);

    // Find by completion status
    List<Visit> findByIsCompletedOrderByVisitDateDesc(Boolean isCompleted);

    // Find by date range
    @Query("SELECT v FROM Visit v WHERE v.visitDate BETWEEN :startDate AND :endDate ORDER BY v.visitDate DESC")
    List<Visit> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find upcoming visits
    @Query("SELECT v FROM Visit v WHERE v.visitDate >= :currentDate AND v.isCompleted = false ORDER BY v.visitDate ASC")
    List<Visit> findUpcomingVisits(@Param("currentDate") LocalDate currentDate);

    // Find past visits
    @Query("SELECT v FROM Visit v WHERE v.visitDate < :currentDate ORDER BY v.visitDate DESC")
    List<Visit> findPastVisits(@Param("currentDate") LocalDate currentDate);

    // Find today's visits
    @Query("SELECT v FROM Visit v WHERE v.visitDate = :date ORDER BY v.startTime ASC")
    List<Visit> findVisitsByDate(@Param("date") LocalDate date);

    // Find visits requiring follow-up
    @Query("SELECT v FROM Visit v WHERE v.followUpRequired = true AND v.followUpDate <= :date AND v.isCompleted = true")
    List<Visit> findVisitsRequiringFollowUp(@Param("date") LocalDate date);

    // Find incomplete past visits
    @Query("SELECT v FROM Visit v WHERE v.visitDate < :currentDate AND v.isCompleted = false ORDER BY v.visitDate DESC")
    List<Visit> findIncompletePastVisits(@Param("currentDate") LocalDate currentDate);

    // Count by type
    Long countByType(VisitType type);

    // Count by completion status
    Long countByIsCompleted(Boolean isCompleted);

    // Count visits by date range
    @Query("SELECT COUNT(v) FROM Visit v WHERE v.visitDate BETWEEN :startDate AND :endDate")
    Long countByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Count upcoming visits
    @Query("SELECT COUNT(v) FROM Visit v WHERE v.visitDate >= :currentDate AND v.isCompleted = false")
    Long countUpcoming(@Param("currentDate") LocalDate currentDate);
}
