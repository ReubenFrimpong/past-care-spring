package com.reuben.pastcare_spring.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

  @Column(nullable = false)
  private boolean active = true;
}
