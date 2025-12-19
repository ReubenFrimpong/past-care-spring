package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

  /**
   * Find location by exact coordinates
   * Used to check if a location already exists before creating a new one
   */
  Optional<Location> findByCoordinates(String coordinates);

  /**
   * Find location by city and optional suburb
   * Useful for deduplication when coordinates might vary slightly
   */
  Optional<Location> findByCityAndSuburb(String city, String suburb);

  /**
   * Check if a location exists with the given coordinates
   */
  boolean existsByCoordinates(String coordinates);
}
