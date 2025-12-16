package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.RefreshToken;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${jwt.refresh-token.expiration:2592000000}") // 30 days default
  private Long refreshTokenDurationMs;

  @Value("${jwt.refresh-token.max-active:5}") // Max 5 active sessions per user
  private int maxActiveTokensPerUser;

  /**
   * Create a new refresh token for a user.
   */
  @Transactional
  public RefreshToken createRefreshToken(User user, Church church, String ipAddress, String userAgent) {
    // Check if user has too many active tokens (sessions)
    long activeTokenCount = refreshTokenRepository.countActiveTokensByUser(user, LocalDateTime.now());

    if (activeTokenCount >= maxActiveTokensPerUser) {
      // Revoke oldest tokens to make room
      logger.info("User {} has {} active tokens, revoking oldest", user.getEmail(), activeTokenCount);
      List<RefreshToken> activeTokens = refreshTokenRepository.findActiveTokensByUser(user, LocalDateTime.now());

      // Keep only the most recent (maxActiveTokensPerUser - 1) tokens
      activeTokens.stream()
          .sorted((t1, t2) -> t2.getLastUsedAt().compareTo(t1.getLastUsedAt()))
          .skip(maxActiveTokensPerUser - 1)
          .forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
          });
    }

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setChurch(church);
    refreshToken.setToken(UUID.randomUUID().toString());
    refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000));
    refreshToken.setRevoked(false);
    refreshToken.setIpAddress(ipAddress);
    refreshToken.setUserAgent(userAgent);
    refreshToken.setLastUsedAt(LocalDateTime.now());

    logger.info("Creating new refresh token for user: {} from IP: {}", user.getEmail(), ipAddress);

    return refreshTokenRepository.save(refreshToken);
  }

  /**
   * Validate and retrieve refresh token.
   */
  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token)
        .filter(RefreshToken::isValid);
  }

  /**
   * Update last used timestamp for refresh token.
   */
  @Transactional
  public void updateLastUsed(RefreshToken token) {
    token.setLastUsedAt(LocalDateTime.now());
    refreshTokenRepository.save(token);
  }

  /**
   * Revoke a specific refresh token.
   */
  @Transactional
  public void revokeToken(String token) {
    refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
      refreshToken.setRevoked(true);
      refreshTokenRepository.save(refreshToken);
      logger.info("Revoked refresh token for user: {}", refreshToken.getUser().getEmail());
    });
  }

  /**
   * Revoke all tokens for a user (logout from all devices).
   */
  @Transactional
  public void revokeAllUserTokens(User user) {
    refreshTokenRepository.revokeAllUserTokens(user);
    logger.info("Revoked all refresh tokens for user: {}", user.getEmail());
  }

  /**
   * Cleanup expired tokens (scheduled task).
   */
  @Transactional
  public void cleanupExpiredTokens() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(7); // Keep for 7 days after expiry for audit
    refreshTokenRepository.deleteExpiredTokens(cutoff);
    logger.info("Cleaned up expired refresh tokens older than {}", cutoff);
  }

  /**
   * Get all active sessions for a user.
   */
  public List<RefreshToken> getActiveUserSessions(User user) {
    return refreshTokenRepository.findActiveTokensByUser(user, LocalDateTime.now());
  }
}
