-- MySQL schema for Dorm Application Management System
-- Author: AAU Dorm Management Team
-- Compatible with MySQL 8.0+

-- Create database
CREATE DATABASE IF NOT EXISTS dormdb;
USE dormdb;

-- Enum types (using VARCHAR in MySQL instead of native ENUMs for flexibility)
-- user_role: OWNER, ADMIN, PROCTOR, STUDENT
-- student_category: NORMAL, STAFF_PRIVILEGED
-- application_status: DRAFT, SUBMITTED, NEEDS_EDIT, UNDER_REVIEW, ACCEPTED, REJECTED, CHECKED_IN, WITHDREW
-- sponsorship_type: GOV, SELF
-- gender: MALE, FEMALE

-- ============================================================================
-- Core Users & Authentication
-- ============================================================================

/**
 * app_user: Central user table for all system users
 * - id: Auto-incremented primary key
 * - username: Unique login identifier
 * - password: User password
 * - role: User role (OWNER, ADMIN, PROCTOR, STUDENT)
 * - active: Account active status
 * - created_at: Account creation timestamp
 */
CREATE TABLE app_user (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  username        VARCHAR(50) UNIQUE NOT NULL,
  password        TEXT NOT NULL,
  role            VARCHAR(20) NOT NULL CHECK (role IN ('OWNER','ADMIN','PROCTOR','STUDENT')),
  active          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/**
 * student_profile: Extended profile for students
 * - user_id: FK to app_user (STUDENT role only)
 * - full_name: Student's full name
 * - aau_id: University ID (unique)
 * - department: Department/Faculty
 * - year_of_study: 1-7 years
 * - category: NORMAL or STAFF_PRIVILEGED (affects allocation priority)
 * - gender: MALE or FEMALE
 */
CREATE TABLE student_profile (
  user_id         BIGINT PRIMARY KEY,
  full_name       VARCHAR(120) NOT NULL,
  aau_id          VARCHAR(30) UNIQUE,
  department      VARCHAR(120),
  year_of_study   INT CHECK (year_of_study BETWEEN 1 AND 7),
  category        VARCHAR(30) NOT NULL DEFAULT 'NORMAL' CHECK (category IN ('NORMAL','STAFF_PRIVILEGED')),
  gender          VARCHAR(10) NOT NULL CHECK (gender IN ('MALE','FEMALE')),
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
  INDEX idx_aau_id (aau_id),
  INDEX idx_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Campus & Housing Infrastructure
-- ============================================================================

/**
 * campus: University campuses/locations
 * - id: Primary key
 * - name: Campus name (unique)
 */
CREATE TABLE campus (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  name            VARCHAR(80) UNIQUE NOT NULL,
  location        VARCHAR(200),
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/**
 * block: Dormitory blocks within a campus
 * - id: Primary key
 * - campus_id: FK to campus
 * - block_code: Block identifier (e.g., "B1", "A2")
 * - gender: MALE or FEMALE designation
 * - total_beds: Total bed capacity
 */
CREATE TABLE block (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  campus_id       BIGINT NOT NULL,
  block_code      VARCHAR(20) NOT NULL,
  gender          VARCHAR(10) NOT NULL DEFAULT 'MALE' CHECK (gender IN ('MALE','FEMALE')),
  total_beds      INT NOT NULL DEFAULT 0,
  available_beds  INT NOT NULL DEFAULT 0,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_block_campus FOREIGN KEY (campus_id) REFERENCES campus(id) ON DELETE CASCADE,
  UNIQUE KEY uk_campus_block (campus_id, block_code),
  INDEX idx_campus (campus_id),
  INDEX idx_gender (gender)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/**
 * bed: Individual beds within blocks
 * - id: Primary key
 * - block_id: FK to block
 * - bed_label: Bed identifier (e.g., "R01-B1", "R02-B2")
 * - is_occupied: Current occupancy status
 * - is_active: Bed available for allocation
 */
CREATE TABLE bed (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  block_id        BIGINT NOT NULL,
  bed_label       VARCHAR(20) NOT NULL,
  is_occupied     BOOLEAN NOT NULL DEFAULT FALSE,
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_bed_block FOREIGN KEY (block_id) REFERENCES block(id) ON DELETE CASCADE,
  UNIQUE KEY uk_block_bed (block_id, bed_label),
  INDEX idx_block (block_id),
  INDEX idx_occupied (is_occupied)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Staff & Operations
-- ============================================================================

/**
 * proctor_assignment: Assigns proctors to blocks for management
 * - proctor_user_id: FK to app_user (PROCTOR role)
 * - block_id: FK to block (block they manage)
 * - active: Assignment active status
 */
CREATE TABLE proctor_assignment (
  proctor_user_id BIGINT PRIMARY KEY,
  block_id        BIGINT NOT NULL,
  active          BOOLEAN NOT NULL DEFAULT TRUE,
  assigned_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_proctor_user FOREIGN KEY (proctor_user_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_proctor_block FOREIGN KEY (block_id) REFERENCES block(id),
  INDEX idx_block (block_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Application & Allocation Management
-- ============================================================================

/**
 * application_window: Time windows for dormitory applications
 * - window_code: Unique window identifier (e.g., "2024-MAIN", "2024-RESIT")
 * - open_at: Application submission opens
 * - close_at: Application submission closes
 * - active: Window currently active
 */
CREATE TABLE application_window (
  window_code     VARCHAR(40) PRIMARY KEY,
  open_at         TIMESTAMP NOT NULL,
  close_at        TIMESTAMP NOT NULL,
  active          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/**
 * dorm_application: Student dormitory applications
 * - id: Primary key
 * - student_user_id: FK to app_user (STUDENT)
 * - window_code: FK to application_window
 * - status: Current application status
 * - sponsorship: GOV or SELF-sponsored
 * - disability: Special needs/disability indicator
 * - department: Student's department
 * - campus_pref: Campus preference
 * - distance_km: Distance from home (km)
 * - notes: Additional notes/requests
 * - score: Allocation priority score
 * - submitted_at: Submission timestamp
 * - updated_at: Last update timestamp
 */
CREATE TABLE dorm_application (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_user_id BIGINT NOT NULL,
  window_code     VARCHAR(40) NOT NULL,
  status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','SUBMITTED','NEEDS_EDIT','UNDER_REVIEW','ACCEPTED','REJECTED','CHECKED_IN','WITHDREW')),
  sponsorship     VARCHAR(10) NOT NULL CHECK (sponsorship IN ('GOV','SELF')),
  disability      BOOLEAN NOT NULL DEFAULT FALSE,
  department      VARCHAR(120),
  campus_pref     VARCHAR(80),
  distance_km     DECIMAL(6,2),
  notes           TEXT,
  score           INT NOT NULL DEFAULT 0,
  submitted_at    TIMESTAMP NULL,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_app_student FOREIGN KEY (student_user_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_app_window FOREIGN KEY (window_code) REFERENCES application_window(window_code),
  UNIQUE KEY uk_student_window (student_user_id, window_code),
  INDEX idx_status (status),
  INDEX idx_window (window_code),
  INDEX idx_score (score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/**
 * application_review: Admin review audit trail
 * - id: Primary key
 * - application_id: FK to dorm_application
 * - reviewer_user_id: FK to app_user (ADMIN/OWNER)
 * - decision: Review decision (APPROVED, REJECTED, etc.)
 * - comment: Reviewer comment
 * - created_at: Review timestamp
 */
CREATE TABLE application_review (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  application_id  BIGINT NOT NULL,
  reviewer_user_id BIGINT NOT NULL,
  decision        VARCHAR(20) NOT NULL,
  comment         TEXT,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_review_app FOREIGN KEY (application_id) REFERENCES dorm_application(id) ON DELETE CASCADE,
  CONSTRAINT fk_review_user FOREIGN KEY (reviewer_user_id) REFERENCES app_user(id),
  INDEX idx_application (application_id),
  INDEX idx_reviewer (reviewer_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/**
 * allocation: Bed allocations to approved students
 * - id: Primary key
 * - application_id: FK to dorm_application (unique - one allocation per application)
 * - bed_id: FK to bed (unique - one student per bed)
 * - assigned_by: FK to app_user (ADMIN/PROCTOR who assigned)
 * - room_number: Room designation (e.g., "R01-201")
 * - allocated_at: Allocation timestamp
 * - checked_in_at: Student check-in timestamp
 * - checked_out_at: Student checkout/withdrawal timestamp
 */
CREATE TABLE allocation (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  application_id  BIGINT UNIQUE NOT NULL,
  bed_id          BIGINT UNIQUE NOT NULL,
  assigned_by     BIGINT NOT NULL,
  room_number     VARCHAR(30),
  allocated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  checked_in_at   TIMESTAMP NULL,
  checked_out_at  TIMESTAMP NULL,
  CONSTRAINT fk_alloc_app FOREIGN KEY (application_id) REFERENCES dorm_application(id) ON DELETE CASCADE,
  CONSTRAINT fk_alloc_bed FOREIGN KEY (bed_id) REFERENCES bed(id),
  CONSTRAINT fk_alloc_user FOREIGN KEY (assigned_by) REFERENCES app_user(id),
  INDEX idx_application (application_id),
  INDEX idx_bed (bed_id),
  INDEX idx_checked_in (checked_in_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Communication & Notifications
-- ============================================================================

/**
 * notification: System notifications sent to users
 * - id: Primary key
 * - to_user_id: FK to app_user (recipient)
 * - title: Notification title
 * - message: Notification message body
 * - is_read: Read status
 * - created_at: Notification timestamp
 */
CREATE TABLE notification (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  to_user_id      BIGINT NOT NULL,
  title           VARCHAR(120) NOT NULL,
  message         TEXT NOT NULL,
  is_read         BOOLEAN NOT NULL DEFAULT FALSE,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notif_user FOREIGN KEY (to_user_id) REFERENCES app_user(id) ON DELETE CASCADE,
  INDEX idx_user (to_user_id),
  INDEX idx_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Audit & Logging
-- ============================================================================

/**
 * audit_log: System action audit trail
 * - id: Primary key
 * - actor_user_id: FK to app_user (who performed action)
 * - action: Action description (e.g., "ALLOCATE_BED", "REJECT_APPLICATION")
 * - entity_type: Entity affected (e.g., "APPLICATION", "ALLOCATION")
 * - entity_id: ID of affected entity
 * - details: Additional details (JSON-compatible)
 * - created_at: Action timestamp
 */
CREATE TABLE audit_log (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  actor_user_id   BIGINT,
  action          VARCHAR(80) NOT NULL,
  entity_type     VARCHAR(80) NOT NULL,
  entity_id       BIGINT,
  details         TEXT,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_audit_user FOREIGN KEY (actor_user_id) REFERENCES app_user(id) ON DELETE SET NULL,
  INDEX idx_action (action),
  INDEX idx_entity (entity_type, entity_id),
  INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Indexes for Performance
-- ============================================================================

-- Already created with table definitions, but adding additional compound indexes
CREATE INDEX idx_app_status_window ON dorm_application(status, window_code);
CREATE INDEX idx_alloc_dates ON allocation(allocated_at, checked_in_at);
CREATE INDEX idx_bed_block_active ON bed(block_id, is_active, is_occupied);

-- ============================================================================
-- Sample Data (for testing)
-- ============================================================================

-- Insert campuses
INSERT INTO campus (name, location) VALUES 
('Main Campus', 'Addis Ababa'),
('North Campus', 'Adama'),
('South Campus', 'Awassa');

-- Get campus IDs for reference
-- @campusMain := (SELECT id FROM campus WHERE name = 'Main Campus');
-- @campusNorth := (SELECT id FROM campus WHERE name = 'North Campus');
-- @campusSouth := (SELECT id FROM campus WHERE name = 'South Campus');

-- Insert application window
INSERT INTO application_window (window_code, open_at, close_at, active) VALUES 
('2024-MAIN', '2024-01-01 00:00:00', '2024-01-31 23:59:59', TRUE),
('2024-RESIT', '2024-06-01 00:00:00', '2024-06-30 23:59:59', FALSE);

-- Create default admin user (password: admin123)
INSERT INTO app_user (username, password, role, active) VALUES 
('admin', 'admin123', 'ADMIN', TRUE);

-- Create sample proctor (password: proctor123)
INSERT INTO app_user (username, password, role, active) VALUES 
('proctor1', 'proctor123', 'PROCTOR', TRUE);

-- Create sample student (password: student123)
INSERT INTO app_user (username, password, role, active) VALUES 
('student1', 'student123', 'STUDENT', TRUE);

-- Add student profile
INSERT INTO student_profile (user_id, full_name, aau_id, department, year_of_study, category, gender) VALUES 
(3, 'John Doe', 'AAU001', 'Computer Science', 2, 'NORMAL', 'MALE');
