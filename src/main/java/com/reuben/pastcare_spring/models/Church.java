package com.reuben.pastcare_spring.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Church entity - The root tenant in the multi-tenant architecture.
 *
 * Each church is a separate tenant with complete data isolation.
 * All other entities (Member, Fellowship, AttendanceSession, etc.) belong to a church.
 *
 * Note: This entity extends BaseEntity (not TenantBaseEntity) because it IS the tenant.
 */
@Entity
@EqualsAndHashCode(callSuper = true)
@Data
public class Church extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String name;

  // Optional: Add more church-specific fields as needed
  private String address;
  private String phoneNumber;
  private String email;
  private String website;
  private String pastor;
  private String denomination;
  private Integer foundedYear;
  private Integer numberOfMembers;

  // Church logo/branding
  private String logoUrl;  // URL or path to uploaded logo

  @Column(nullable = false)
  private boolean active = true;

  /**
   * Denormalized storage limit cache (in MB)
   * Base 2048 MB (2GB) + sum of active addon storage
   * Updated only when addons are purchased/canceled (rare operations)
   * Prevents N+1 queries on every file upload validation
   */
  @Column(name = "total_storage_limit_mb", nullable = false)
  private Long totalStorageLimitMb = 2048L; // Default base 2GB

  /**
   * Last time storage limit was recalculated
   * Updated when addon is purchased, canceled, or suspended
   */
  @Column(name = "storage_limit_updated_at")
  private LocalDateTime storageLimitUpdatedAt;
}
