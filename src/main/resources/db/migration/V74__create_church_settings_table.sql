-- Create church_settings table for storing church-specific configuration

CREATE TABLE church_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT,
    setting_type VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key constraint
    FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,

    -- Unique constraint: one key per church
    UNIQUE KEY unique_church_setting (church_id, setting_key),

    -- Index for faster lookups
    INDEX idx_church_settings_church_id (church_id),
    INDEX idx_church_settings_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default notification settings for all existing churches
INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'emailNotifications', 'true', 'BOOLEAN' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'emailNotifications'
);

INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'smsNotifications', 'true', 'BOOLEAN' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'smsNotifications'
);

INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'eventReminders', 'true', 'BOOLEAN' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'eventReminders'
);

INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'birthdayReminders', 'true', 'BOOLEAN' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'birthdayReminders'
);

INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'anniversaryReminders', 'true', 'BOOLEAN' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'anniversaryReminders'
);

INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'donationReceipts', 'true', 'BOOLEAN' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'donationReceipts'
);

INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'attendanceReports', 'false', 'BOOLEAN' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'attendanceReports'
);

-- Insert default system preferences for all existing churches
INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'defaultEventDuration', '120', 'NUMBER' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'defaultEventDuration'
);

INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'attendanceGracePeriod', '15', 'NUMBER' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'attendanceGracePeriod'
);

INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'autoApprovePortalRegistrations', 'false', 'BOOLEAN' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'autoApprovePortalRegistrations'
);

INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'requirePhotoForMembers', 'false', 'BOOLEAN' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'requirePhotoForMembers'
);

INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'enablePublicPortal', 'true', 'BOOLEAN' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'enablePublicPortal'
);

INSERT INTO church_settings (church_id, setting_key, setting_value, setting_type)
SELECT id, 'allowSelfCheckIn', 'true', 'BOOLEAN' FROM church
WHERE NOT EXISTS (
    SELECT 1 FROM church_settings
    WHERE church_settings.church_id = church.id
    AND church_settings.setting_key = 'allowSelfCheckIn'
);
