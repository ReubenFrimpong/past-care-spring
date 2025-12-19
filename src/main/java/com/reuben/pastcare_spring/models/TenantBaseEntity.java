package com.reuben.pastcare_spring.models;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * Base entity for all tenant-scoped entities in the multi-tenant SaaS architecture.
 *
 * <p>Automatically filters ALL queries by churchId (tenant) using Hibernate filters.
 * This provides defense-in-depth security by ensuring data isolation at the ORM level.
 *
 * <p><b>Entities extending this class:</b>
 * <ul>
 *   <li>Member - Church members data</li>
 *   <li>Fellowship - Church fellowship groups</li>
 *   <li>AttendanceSession - Attendance tracking sessions</li>
 *   <li>And any future tenant-scoped entities</li>
 * </ul>
 *
 * <p><b>How it works:</b>
 * <ol>
 *   <li>Every entity has a required church_id foreign key</li>
 *   <li>Hibernate @Filter automatically adds "WHERE church_id = :churchId" to all queries</li>
 *   <li>TenantContext.setCurrentChurchId() sets the filter parameter from JWT</li>
 *   <li>All queries are automatically scoped to the current tenant</li>
 * </ol>
 *
 * <p><b>Security guarantees:</b>
 * - Users cannot query data from other churches
 * - Cross-tenant data leaks are prevented at the database level
 * - Even if application logic fails, the filter provides protection
 *
 * @see Church - The root tenant entity
 * @see com.reuben.pastcare_spring.security.TenantContext - Thread-local tenant storage
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@FilterDef(name = "churchFilter", parameters = @ParamDef(name = "churchId", type = Long.class))
@Filter(name = "churchFilter", condition = "church_id = :churchId")
public abstract class TenantBaseEntity extends BaseEntity {

  /**
   * The church (tenant) this entity belongs to.
   * Required(for non SUPERADMINS) and immutable after creation for data integrity.
   */
  @ManyToOne
  @JoinColumn(name = "church_id", nullable = false, updatable = false)
  private Church church;

  /**
   * Convenience method to get the church ID.
   * Useful for logging, validation, and debugging.
   *
   * @return The church ID, or null if church is not set
   */
  public Long getChurchId() {
    return church != null ? church.getId() : null;
  }
}
