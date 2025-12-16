package com.reuben.pastcare_spring.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reuben.pastcare_spring.models.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
  List<Attendance> findByAttendanceSessionId(Long attendanceSessionId);
  List<Attendance> findByMemberId(Long memberId);
  Optional<Attendance> findByMemberIdAndAttendanceSessionId(Long memberId, Long attendanceSessionId);
}
