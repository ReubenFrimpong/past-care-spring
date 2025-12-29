package com.reuben.pastcare_spring.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-local storage for current tenant (church) ID, user ID, and role.
 * This allows tenant-scoped data access throughout the request lifecycle.
 */
public class TenantContext {

  private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);
  private static final ThreadLocal<Long> currentChurchId = new ThreadLocal<>();
  private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
  private static final ThreadLocal<String> currentUserRole = new ThreadLocal<>();

  public static void setCurrentChurchId(Long churchId) {
    logger.debug("Setting tenant context: churchId = {}", churchId);
    currentChurchId.set(churchId);
  }

  public static Long getCurrentChurchId() {
    return currentChurchId.get();
  }

  public static void setCurrentUserId(Long userId) {
    logger.debug("Setting user context: userId = {}", userId);
    currentUserId.set(userId);
  }

  public static Long getCurrentUserId() {
    return currentUserId.get();
  }

  public static void setCurrentUserRole(String role) {
    logger.debug("Setting user role: role = {}", role);
    currentUserRole.set(role);
  }

  public static String getCurrentUserRole() {
    return currentUserRole.get();
  }

  public static boolean isSuperadmin() {
    return "SUPERADMIN".equals(currentUserRole.get());
  }

  public static void clear() {
    logger.debug("Clearing tenant context");
    currentChurchId.remove();
    currentUserId.remove();
    currentUserRole.remove();
  }

  public static boolean isSet() {
    return currentChurchId.get() != null;
  }
}
