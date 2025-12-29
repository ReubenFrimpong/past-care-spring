package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.enums.WidgetCategory;
import com.reuben.pastcare_spring.models.Widget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Widget entity.
 * Dashboard Phase 2.1: Custom Layouts MVP
 */
@Repository
public interface WidgetRepository extends JpaRepository<Widget, Long> {

    /**
     * Find all active widgets
     */
    List<Widget> findByIsActiveTrue();

    /**
     * Find active widgets by category
     */
    List<Widget> findByCategoryAndIsActiveTrue(WidgetCategory category);

    /**
     * Find widget by unique key
     */
    Optional<Widget> findByWidgetKey(String widgetKey);
}
