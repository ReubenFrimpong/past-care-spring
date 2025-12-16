package com.reuben.pastcare_spring.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-local storage for current tenant (church) ID.
 * This allows tenant-scoped data access throughout the request lifecycle.
 */
public class TenantContext {

  private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);
  private static final ThreadLocal<Long> currentChurchId = new ThreadLocal<>();

  public static void setCurrentChurchId(Long churchId) {
    logger.debug("Setting tenant context: churchId = {}", churchId);
    currentChurchId.set(churchId);
  }

  public static Long getCurrentChurchId() {
    return currentChurchId.get();
  }

  public static void clear() {
    logger.debug("Clearing tenant context");
    currentChurchId.remove();
  }

  public static boolean isSet() {
    return currentChurchId.get() != null;
  }
}
