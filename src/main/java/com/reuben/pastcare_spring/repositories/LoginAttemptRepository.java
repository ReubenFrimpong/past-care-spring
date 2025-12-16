package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

  @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = :email AND la.success = false AND la.attemptTime > :since")
  long countFailedAttemptsByEmailSince(@Param("email") String email, @Param("since") LocalDateTime since);

  @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.success = false AND la.attemptTime > :since")
  long countFailedAttemptsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

  List<LoginAttempt> findByEmailAndAttemptTimeAfterOrderByAttemptTimeDesc(String email, LocalDateTime since);

  void deleteByAttemptTimeBefore(LocalDateTime before);
}
