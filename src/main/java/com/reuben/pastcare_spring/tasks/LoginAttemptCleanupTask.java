package com.reuben.pastcare_spring.tasks;

import com.reuben.pastcare_spring.services.BruteForceProtectionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginAttemptCleanupTask {

  private static final Logger logger = LoggerFactory.getLogger(LoginAttemptCleanupTask.class);
  private final BruteForceProtectionService bruteForceProtectionService;

  /**
   * Cleanup old login attempts daily at 2 AM
   * Cron expression: second, minute, hour, day of month, month, day of week
   */
  @Scheduled(cron = "0 0 2 * * *")
  public void cleanupOldLoginAttempts() {
    logger.info("Starting scheduled cleanup of old login attempts...");
    bruteForceProtectionService.cleanupOldAttempts();
    logger.info("Completed cleanup of old login attempts.");
  }
}
