package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.StorageAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageAddonRepository extends JpaRepository<StorageAddon, Long> {

    /**
     * Find all active storage add-ons ordered by display order
     */
    List<StorageAddon> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find storage add-on by name
     */
    java.util.Optional<StorageAddon> findByName(String name);
}
