package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.ChurchSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ChurchSettings entity
 */
@Repository
public interface ChurchSettingsRepository extends JpaRepository<ChurchSettings, Long> {

    /**
     * Find all settings for a specific church
     */
    List<ChurchSettings> findByChurchId(Long churchId);

    /**
     * Find a specific setting by church and key
     */
    Optional<ChurchSettings> findByChurchIdAndSettingKey(Long churchId, String settingKey);

    /**
     * Delete all settings for a church
     */
    void deleteByChurchId(Long churchId);
}
