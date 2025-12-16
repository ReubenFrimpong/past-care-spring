package com.reuben.pastcare_spring.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh token for maintaining user sessions without storing JWT.
 * Allows token rotation and revocation.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "refresh_tokens")
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken extends BaseEntity {

  @Column(nullable = false, unique = true, length = 500)
  private String token;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "church_id", nullable = false)
  private Church church;

  @Column(nullable = false)
  private LocalDateTime expiryDate;

  @Column(nullable = false)
  private boolean revoked = false;

  @Column(nullable = false)
  private String ipAddress;

  private String userAgent;

  @Column(nullable = false)
  private LocalDateTime lastUsedAt;

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiryDate);
  }

  public boolean isValid() {
    return !revoked && !isExpired();
  }
}
