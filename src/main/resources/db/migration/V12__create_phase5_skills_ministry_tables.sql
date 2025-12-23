-- Phase 5: Skills & Ministry Involvement
-- Creates tables for skills registry, member skills, and ministry management

-- Table: skills
-- Stores available skills that members can have
CREATE TABLE skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    church_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_skill_name_church UNIQUE (name, church_id),
    INDEX idx_skills_church (church_id),
    INDEX idx_skills_category (category),
    INDEX idx_skills_active (is_active)
);

-- Table: member_skills
-- Join table connecting members to skills with proficiency levels
CREATE TABLE member_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    proficiency_level VARCHAR(20) NOT NULL,
    willing_to_serve BOOLEAN NOT NULL DEFAULT TRUE,
    currently_serving BOOLEAN NOT NULL DEFAULT FALSE,
    years_of_experience INT,
    notes TEXT,
    acquired_date TIMESTAMP,
    last_verified_date TIMESTAMP,
    church_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_member_skill UNIQUE (member_id, skill_id),
    CONSTRAINT fk_member_skills_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    INDEX idx_member_skills_church (church_id),
    INDEX idx_member_skills_member (member_id),
    INDEX idx_member_skills_skill (skill_id),
    INDEX idx_member_skills_proficiency (proficiency_level),
    INDEX idx_member_skills_willing (willing_to_serve),
    INDEX idx_member_skills_serving (currently_serving)
);

-- Table: ministries
-- Stores church ministries/departments
CREATE TABLE ministries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    leader_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    meeting_schedule VARCHAR(200),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    church_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_ministry_name_church UNIQUE (name, church_id),
    CONSTRAINT fk_ministries_leader FOREIGN KEY (leader_id) REFERENCES members(id) ON DELETE SET NULL,
    INDEX idx_ministries_church (church_id),
    INDEX idx_ministries_leader (leader_id),
    INDEX idx_ministries_status (status)
);

-- Table: ministry_skills
-- Join table for skills required/relevant for each ministry
CREATE TABLE ministry_skills (
    ministry_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,

    PRIMARY KEY (ministry_id, skill_id),
    CONSTRAINT fk_ministry_skills_ministry FOREIGN KEY (ministry_id) REFERENCES ministries(id) ON DELETE CASCADE,
    CONSTRAINT fk_ministry_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    INDEX idx_ministry_skills_skill (skill_id)
);

-- Table: ministry_members
-- Join table for members assigned to each ministry
CREATE TABLE ministry_members (
    ministry_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,

    PRIMARY KEY (ministry_id, member_id),
    CONSTRAINT fk_ministry_members_ministry FOREIGN KEY (ministry_id) REFERENCES ministries(id) ON DELETE CASCADE,
    CONSTRAINT fk_ministry_members_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    INDEX idx_ministry_members_member (member_id)
);

-- Insert default skills for common church activities
INSERT INTO skills (name, description, category, church_id) VALUES
-- Music & Worship
('Singing', 'Vocal performance in choir or worship team', 'MUSIC_VOCAL', 1),
('Piano', 'Piano keyboard playing', 'MUSIC_INSTRUMENTAL', 1),
('Guitar', 'Acoustic or electric guitar playing', 'MUSIC_INSTRUMENTAL', 1),
('Drums', 'Drum kit and percussion', 'MUSIC_INSTRUMENTAL', 1),
('Sound Engineering', 'Audio mixing and sound system operation', 'MUSIC_PRODUCTION', 1),

-- Technical & Media
('Photography', 'Event and portrait photography', 'PHOTOGRAPHY', 1),
('Video Editing', 'Video production and editing', 'VIDEOGRAPHY', 1),
('Graphic Design', 'Creating visual content and graphics', 'GRAPHIC_DESIGN', 1),
('IT Support', 'Computer and network troubleshooting', 'WEB_TECH', 1),

-- Teaching & Education
('Bible Teaching', 'Teaching biblical principles', 'TEACHING', 1),
('Sunday School', 'Teaching children and youth', 'CHILDREN_MINISTRY', 1),
('Counseling', 'Pastoral care and counseling', 'COUNSELING', 1),

-- Administrative
('Administration', 'Office management and organization', 'ADMINISTRATION', 1),
('Bookkeeping', 'Financial record keeping', 'FINANCE', 1),
('Event Planning', 'Organizing and coordinating events', 'EVENT_PLANNING', 1),

-- Hospitality & Service
('Ushering', 'Welcoming and guiding attendees', 'HOSPITALITY', 1),
('Cooking', 'Food preparation for church events', 'CATERING', 1),
('Cleaning', 'Facilities maintenance and cleaning', 'CLEANING', 1),
('Driving', 'Transportation and logistics', 'TRANSPORTATION', 1),

-- Outreach
('Evangelism', 'Sharing the gospel', 'EVANGELISM', 1),
('Social Media', 'Managing social media accounts', 'SOCIAL_MEDIA', 1);
