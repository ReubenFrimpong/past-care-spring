package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.ChurchSettings;
import com.reuben.pastcare_spring.repositories.ChurchSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing church settings
 */
@Service
@RequiredArgsConstructor
public class ChurchSettingsService {

    private final ChurchSettingsRepository churchSettingsRepository;

    /**
     * Get all settings for a church as a map
     */
    public Map<String, String> getSettings(Long churchId) {
        List<ChurchSettings> settings = churchSettingsRepository.findByChurchId(churchId);
        Map<String, String> settingsMap = new HashMap<>();
        for (ChurchSettings setting : settings) {
            settingsMap.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return settingsMap;
    }

    /**
     * Get a single setting value
     */
    public String getSetting(Long churchId, String key) {
        return churchSettingsRepository.findByChurchIdAndSettingKey(churchId, key)
                .map(ChurchSettings::getSettingValue)
                .orElse(null);
    }

    /**
     * Get a boolean setting
     */
    public boolean getBooleanSetting(Long churchId, String key, boolean defaultValue) {
        String value = getSetting(churchId, key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Get an integer setting
     */
    public Integer getIntegerSetting(Long churchId, String key, Integer defaultValue) {
        String value = getSetting(churchId, key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Save or update a setting
     */
    @Transactional
    public void saveSetting(Long churchId, String key, String value) {
        ChurchSettings setting = churchSettingsRepository.findByChurchIdAndSettingKey(churchId, key)
                .orElse(new ChurchSettings());

        setting.setChurchId(churchId);
        setting.setSettingKey(key);
        setting.setSettingValue(value);

        // Auto-detect type
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            setting.setSettingType("BOOLEAN");
        } else if (value.matches("-?\\d+")) {
            setting.setSettingType("NUMBER");
        } else if (value.startsWith("{") || value.startsWith("[")) {
            setting.setSettingType("JSON");
        } else {
            setting.setSettingType("STRING");
        }

        churchSettingsRepository.save(setting);
    }

    /**
     * Save multiple settings at once
     */
    @Transactional
    public void saveSettings(Long churchId, Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            saveSetting(churchId, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Delete a setting
     */
    @Transactional
    public void deleteSetting(Long churchId, String key) {
        churchSettingsRepository.findByChurchIdAndSettingKey(churchId, key)
                .ifPresent(churchSettingsRepository::delete);
    }

    /**
     * Delete all settings for a church
     */
    @Transactional
    public void deleteAllSettings(Long churchId) {
        churchSettingsRepository.deleteByChurchId(churchId);
    }
}
