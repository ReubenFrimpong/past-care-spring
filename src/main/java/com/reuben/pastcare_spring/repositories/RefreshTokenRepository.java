package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.RefreshToken;
import com.reuben.pastcare_spring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  List<RefreshToken> findByUser(User user);

  @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiryDate > :now")
  List<RefreshToken> findActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

  @Modifying
  @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
  void revokeAllUserTokens(@Param("user") User user);

  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :expiryDate")
  void deleteExpiredTokens(@Param("expiryDate") LocalDateTime expiryDate);

  @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiryDate > :now")
  long countActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);
}
