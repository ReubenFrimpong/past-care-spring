package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.DashboardLayout;
import com.reuben.pastcare_spring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DashboardLayout entity.
 * Dashboard Phase 2.1: Custom Layouts MVP
 */
@Repository
public interface DashboardLayoutRepository extends JpaRepository<DashboardLayout, Long> {

    /**
     * Find user's default layout
     */
    Optional<DashboardLayout> findByUserAndIsDefaultTrue(User user);

    /**
     * Find all layouts for a user
     */
    List<DashboardLayout> findByUser(User user);

    /**
     * Find a specific layout by user and ID
     */
    Optional<DashboardLayout> findByUserAndId(User user, Long id);
}
