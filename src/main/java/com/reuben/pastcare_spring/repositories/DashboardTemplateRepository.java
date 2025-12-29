package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.models.DashboardTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing dashboard templates.
 * Provides queries for finding templates by role, default templates, etc.
 */
@Repository
public interface DashboardTemplateRepository extends JpaRepository<DashboardTemplate, Long> {

    /**
     * Find all templates for a specific role
     */
    List<DashboardTemplate> findByRole(Role role);

    /**
     * Find all templates for a specific role, ordered by name
     */
    List<DashboardTemplate> findByRoleOrderByTemplateNameAsc(Role role);

    /**
     * Find the default template for a specific role
     */
    Optional<DashboardTemplate> findByRoleAndIsDefaultTrue(Role role);

    /**
     * Find all default templates (one per role)
     */
    List<DashboardTemplate> findByIsDefaultTrue();

    /**
     * Check if a template name already exists for a role
     */
    boolean existsByRoleAndTemplateNameIgnoreCase(Role role, String templateName);

    /**
     * Find templates by role with optional filtering
     */
    @Query("SELECT t FROM DashboardTemplate t WHERE " +
           "(:role IS NULL OR t.role = :role) AND " +
           "(:isDefault IS NULL OR t.isDefault = :isDefault)")
    List<DashboardTemplate> findTemplatesWithFilters(
        @Param("role") Role role,
        @Param("isDefault") Boolean isDefault
    );

    /**
     * Count templates for a specific role
     */
    long countByRole(Role role);

    /**
     * Find all templates accessible by a user based on their role
     * Returns templates for the user's role plus templates marked for lower privilege levels
     */
    @Query("SELECT t FROM DashboardTemplate t WHERE t.role = :role ORDER BY t.isDefault DESC, t.templateName ASC")
    List<DashboardTemplate> findTemplatesForRole(@Param("role") Role role);
}
