package com.reuben.pastcare_spring.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.reuben.pastcare_spring.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing a user invitation to join a church.
 * Invitations are sent by admins to allow new users to register and be automatically associated with their church.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Invitation extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "church_id", nullable = false)
  private Church church;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false, unique = true)
  private String token;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  private Role role;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  private boolean used = false;

  @ManyToOne
  @JoinColumn(name = "invited_by_user_id")
  private User invitedBy;

  @ManyToOne
  @JoinColumn(name = "registered_user_id")
  private User registeredUser;

  private LocalDateTime usedAt;

  /**
   * Generate a unique invitation token before persisting.
   */
  @PrePersist
  protected void generateToken() {
    if (this.token == null) {
      this.token = UUID.randomUUID().toString();
    }
  }

  /**
   * Check if the invitation is expired.
   */
  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }

  /**
   * Check if the invitation is valid (not used and not expired).
   */
  public boolean isValid() {
    return !used && !isExpired();
  }

  /**
   * Mark the invitation as used by a registered user.
   */
  public void markAsUsed(User user) {
    this.used = true;
    this.registeredUser = user;
    this.usedAt = LocalDateTime.now();
  }
}
