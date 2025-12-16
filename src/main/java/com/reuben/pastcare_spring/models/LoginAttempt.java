package com.reuben.pastcare_spring.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Login attempt tracking for security and brute force protection.
 *
 * Multi-Tenancy: INTENTIONALLY CROSS-TENANT
 * This security/audit table is not scoped to a specific church.
 * It tracks all login attempts across the entire platform for:
 * - IP-based rate limiting (prevents attacks across multiple churches)
 * - Account lockout (email-based, regardless of church)
 * - Security auditing and monitoring
 *
 * Note: Extends BaseEntity (not TenantBaseEntity) by design.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "login_attempts")
@AllArgsConstructor
@NoArgsConstructor
public class LoginAttempt extends BaseEntity {

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String ipAddress;

  @Column(nullable = false)
  private boolean success;

  @Column(nullable = false)
  private LocalDateTime attemptTime;

  private String userAgent;
}
