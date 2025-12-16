package com.reuben.pastcare_spring.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reuben.pastcare_spring.models.AttendanceSession;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
  List<AttendanceSession> findByChurch_Id(Long churchId);
  List<AttendanceSession> findByFellowship_Id(Long fellowshipId);
  List<AttendanceSession> findBySessionDateBetween(LocalDate startDate, LocalDate endDate);
  List<AttendanceSession> findByChurch_IdAndSessionDateBetween(Long churchId, LocalDate startDate, LocalDate endDate);
}
