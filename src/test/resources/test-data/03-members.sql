-- Seed data for test members
-- Diverse member profiles for testing member management functionality

-- Alpha Test Church Members (Church ID: 1)
INSERT INTO members (id, first_name, middle_name, last_name, phone_number, email, sex, date_of_birth, marital_status, member_status, occupation, address, city, country, profile_picture_url, notes, church_id, created_at, updated_at)
VALUES
    -- Active Members
    (1, 'John', 'David', 'Doe', '+254711111001', 'john.doe@example.com', 'MALE', '1985-05-15', 'MARRIED', 'MEMBER', 'Software Engineer', '10 Main St', 'Nairobi', 'Kenya', null, 'Active member since 2020', 1, NOW(), NOW()),
    (2, 'Jane', 'Mary', 'Smith', '+254711111002', 'jane.smith@example.com', 'FEMALE', '1987-08-22', 'MARRIED', 'MEMBER', 'Teacher', '20 Oak Ave', 'Nairobi', 'Kenya', null, 'Spouse of John Doe', 1, NOW(), NOW()),
    (3, 'Robert', null, 'Johnson', '+254711111003', 'robert.j@example.com', 'MALE', '1990-03-10', 'SINGLE', 'MEMBER', 'Accountant', '30 Pine Rd', 'Nairobi', 'Kenya', null, null, 1, NOW(), NOW()),
    (4, 'Sarah', 'Grace', 'Williams', '+254711111004', 'sarah.w@example.com', 'FEMALE', '1992-11-28', 'SINGLE', 'MEMBER', 'Nurse', '40 Elm St', 'Nairobi', 'Kenya', null, null, 1, NOW(), NOW()),

    -- Visitors
    (5, 'Michael', 'James', 'Brown', '+254711111005', 'michael.b@example.com', 'MALE', '1988-07-05', 'MARRIED', 'VISITOR', 'Business Owner', '50 Cedar Ln', 'Nairobi', 'Kenya', null, 'First time visitor', 1, NOW(), NOW()),
    (6, 'Emily', null, 'Davis', '+254711111006', 'emily.d@example.com', 'FEMALE', '1995-02-14', 'SINGLE', 'VISITOR', 'Student', '60 Birch Dr', 'Nairobi', 'Kenya', null, 'Visited twice', 1, NOW(), NOW()),

    -- Inactive Member
    (7, 'David', 'Paul', 'Wilson', '+254711111007', 'david.w@example.com', 'MALE', '1980-09-20', 'DIVORCED', 'INACTIVE', 'Engineer', '70 Maple Ave', 'Nairobi', 'Kenya', null, 'Moved to another city', 1, NOW(), NOW()),

    -- Children (for household/relationship testing)
    (8, 'Emma', null, 'Doe', '+254711111008', null, 'FEMALE', '2010-04-12', 'SINGLE', 'MEMBER', 'Student', '10 Main St', 'Nairobi', 'Kenya', null, 'Child of John and Jane Doe', 1, NOW(), NOW()),
    (9, 'Lucas', null, 'Doe', '+254711111009', null, 'MALE', '2012-09-08', 'SINGLE', 'MEMBER', 'Student', '10 Main St', 'Nairobi', 'Kenya', null, 'Child of John and Jane Doe', 1, NOW(), NOW()),

    -- New Converts
    (10, 'Olivia', 'Grace', 'Martinez', '+254711111010', 'olivia.m@example.com', 'FEMALE', '1998-12-25', 'SINGLE', 'NEW_CONVERT', 'Marketing Specialist', '80 Spruce St', 'Nairobi', 'Kenya', null, 'Accepted Christ 2 months ago', 1, NOW(), NOW());

-- Beta Test Church Members (Church ID: 2)
INSERT INTO members (id, first_name, middle_name, last_name, phone_number, email, sex, date_of_birth, marital_status, member_status, occupation, address, city, country, profile_picture_url, notes, church_id, created_at, updated_at)
VALUES
    (11, 'Daniel', null, 'Anderson', '+254712111001', 'daniel.a@example.com', 'MALE', '1986-06-18', 'MARRIED', 'MEMBER', 'Doctor', '90 Willow Way', 'Mombasa', 'Kenya', null, 'Member of Beta Church', 2, NOW(), NOW()),
    (12, 'Sophia', 'Joy', 'Taylor', '+254712111002', 'sophia.t@example.com', 'FEMALE', '1989-01-30', 'SINGLE', 'MEMBER', 'Lawyer', '100 Ash Blvd', 'Mombasa', 'Kenya', null, null, 2, NOW(), NOW());

-- Gamma Test Church Members (Church ID: 3)
INSERT INTO members (id, first_name, middle_name, last_name, phone_number, email, sex, date_of_birth, marital_status, member_status, occupation, address, city, country, profile_picture_url, notes, church_id, created_at, updated_at)
VALUES
    (13, 'James', 'Peter', 'Thomas', '+254713111001', 'james.t@example.com', 'MALE', '1983-10-07', 'MARRIED', 'MEMBER', 'Pastor', '110 Oak Ridge', 'Kisumu', 'Kenya', null, 'Member of Gamma Church', 3, NOW(), NOW());

-- Note: Member IDs are explicitly set for predictable testing
-- Members 1-10: Alpha Church (for most tests)
-- Members 11-12: Beta Church (for multi-tenancy isolation)
-- Member 13: Gamma Church (for cross-church access denial)
