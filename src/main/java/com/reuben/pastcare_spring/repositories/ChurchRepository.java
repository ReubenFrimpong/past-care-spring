package com.reuben.pastcare_spring.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reuben.pastcare_spring.models.Church;

public interface ChurchRepository extends JpaRepository<Church, Long> {

  /**
   * Find a church by name (case-insensitive).
   */
  Optional<Church> findByNameIgnoreCase(String name);

  /**
   * Check if a church with the given name exists (case-insensitive).
   */
  boolean existsByNameIgnoreCase(String name);

  /**
   * Find a church by email.
   */
  Optional<Church> findByEmail(String email);
}
