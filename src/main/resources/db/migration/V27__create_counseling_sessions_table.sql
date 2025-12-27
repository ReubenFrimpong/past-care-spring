-- Create counseling_session table for Phase 2: Counseling Sessions
CREATE TABLE counseling_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    church_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    counselor_id BIGINT NOT NULL,
    care_need_id BIGINT,
    title VARCHAR(200) NOT NULL,
    session_notes TEXT,
    type VARCHAR(50),
    session_date DATETIME NOT NULL,
    duration_minutes INT,
    location VARCHAR(200),
    status VARCHAR(30) DEFAULT 'SCHEDULED',

    -- Referral fields
    is_referral_needed BOOLEAN DEFAULT FALSE,
    referred_to VARCHAR(200),
    referral_organization VARCHAR(100),
    referral_phone VARCHAR(20),
    referral_notes TEXT,
    referral_date DATETIME,

    -- Follow-up
    follow_up_required BOOLEAN DEFAULT FALSE,
    follow_up_date DATETIME,
    follow_up_notes TEXT,

    -- Confidentiality
    is_confidential BOOLEAN DEFAULT TRUE NOT NULL,

    -- Outcome
    outcome TEXT,
    session_outcome VARCHAR(30),

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,

    -- Foreign keys
    CONSTRAINT fk_counseling_church FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,
    CONSTRAINT fk_counseling_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_counseling_counselor FOREIGN KEY (counselor_id) REFERENCES user(id) ON DELETE RESTRICT,
    CONSTRAINT fk_counseling_care_need FOREIGN KEY (care_need_id) REFERENCES care_need(id) ON DELETE SET NULL
);

-- Indexes for performance
CREATE INDEX idx_counseling_church ON counseling_session(church_id);
CREATE INDEX idx_counseling_member ON counseling_session(member_id);
CREATE INDEX idx_counseling_counselor ON counseling_session(counselor_id);
CREATE INDEX idx_counseling_care_need ON counseling_session(care_need_id);
CREATE INDEX idx_counseling_session_date ON counseling_session(session_date);
CREATE INDEX idx_counseling_status ON counseling_session(status);
CREATE INDEX idx_counseling_type ON counseling_session(type);
CREATE INDEX idx_counseling_follow_up ON counseling_session(follow_up_date);
