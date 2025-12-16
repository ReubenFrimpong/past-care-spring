package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.LoginAttempt;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.LoginAttemptRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BruteForceProtectionService {

  private static final Logger logger = LoggerFactory.getLogger(BruteForceProtectionService.class);

  private final LoginAttemptRepository loginAttemptRepository;
  private final UserRepository userRepository;

  // Configuration constants
  private static final int MAX_FAILED_ATTEMPTS = 5;
  private static final int LOCKOUT_DURATION_MINUTES = 15;
  private static final int ATTEMPT_WINDOW_MINUTES = 15;
  private static final int MAX_IP_ATTEMPTS = 10;

  @Transactional
  public void recordLoginAttempt(String email, String ipAddress, String userAgent, boolean success) {
    LoginAttempt attempt = new LoginAttempt();
    attempt.setEmail(email);
    attempt.setIpAddress(ipAddress);
    attempt.setUserAgent(userAgent);
    attempt.setSuccess(success);
    attempt.setAttemptTime(LocalDateTime.now());

    loginAttemptRepository.save(attempt);

    if (!success) {
      handleFailedAttempt(email);
      logger.warn("Failed login attempt for email: {} from IP: {}", email, ipAddress);
    } else {
      resetFailedAttempts(email);
      logger.info("Successful login for email: {} from IP: {}", email, ipAddress);
    }
  }

  @Transactional
  public void handleFailedAttempt(String email) {
    Optional<User> userOptional = userRepository.findByEmail(email);

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      int currentAttempts = user.getFailedLoginAttempts() + 1;
      user.setFailedLoginAttempts(currentAttempts);

      if (currentAttempts >= MAX_FAILED_ATTEMPTS) {
        user.setAccountLocked(true);
        user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
        logger.warn("Account locked for email: {} due to {} failed attempts. Locked until: {}",
            email, currentAttempts, user.getAccountLockedUntil());
      }

      userRepository.save(user);
    }
  }

  @Transactional
  public void resetFailedAttempts(String email) {
    Optional<User> userOptional = userRepository.findByEmail(email);

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      user.setFailedLoginAttempts(0);
      user.setAccountLocked(false);
      user.setAccountLockedUntil(null);
      userRepository.save(user);
    }
  }

  public boolean isAccountLocked(String email) {
    Optional<User> userOptional = userRepository.findByEmail(email);

    if (userOptional.isEmpty()) {
      return false;
    }

    User user = userOptional.get();

    // Check if account is locked and if lockout period has expired
    if (user.isAccountLocked()) {
      LocalDateTime lockedUntil = user.getAccountLockedUntil();

      if (lockedUntil != null && LocalDateTime.now().isAfter(lockedUntil)) {
        // Lockout period expired, unlock account
        resetFailedAttempts(email);
        logger.info("Account lockout expired for email: {}", email);
        return false;
      }

      return true;
    }

    return false;
  }

  public boolean isIpBlocked(String ipAddress) {
    LocalDateTime since = LocalDateTime.now().minusMinutes(ATTEMPT_WINDOW_MINUTES);
    long failedAttempts = loginAttemptRepository.countFailedAttemptsByIpSince(ipAddress, since);

    if (failedAttempts >= MAX_IP_ATTEMPTS) {
      logger.warn("IP address {} has {} failed attempts in the last {} minutes",
          ipAddress, failedAttempts, ATTEMPT_WINDOW_MINUTES);
      return true;
    }

    return false;
  }

  public long getFailedAttemptsCount(String email) {
    LocalDateTime since = LocalDateTime.now().minusMinutes(ATTEMPT_WINDOW_MINUTES);
    return loginAttemptRepository.countFailedAttemptsByEmailSince(email, since);
  }

  public LocalDateTime getAccountLockedUntil(String email) {
    Optional<User> userOptional = userRepository.findByEmail(email);
    return userOptional.map(User::getAccountLockedUntil).orElse(null);
  }

  public String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    return request.getRemoteAddr();
  }

  @Transactional
  public void cleanupOldAttempts() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
    loginAttemptRepository.deleteByAttemptTimeBefore(cutoff);
    logger.info("Cleaned up login attempts older than {}", cutoff);
  }
}
