-- MySQL Schema for Dormitory Management System
-- Simple structure for easy understanding
-- Database: dormdb

CREATE DATABASE IF NOT EXISTS dormdb;
USE dormdb;

-- ============================================================================
-- Users Table (All system users: STUDENT, ADMIN, PROCTOR, OWNER)
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
  id              VARCHAR(36) PRIMARY KEY,
  username        VARCHAR(50) UNIQUE NOT NULL,
  password        VARCHAR(255) NOT NULL,
  role            VARCHAR(20) NOT NULL,
  display_name    VARCHAR(120) NOT NULL,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- Students Table (Extended profile for students)
-- ============================================================================
CREATE TABLE IF NOT EXISTS students (
  user_id             VARCHAR(36) PRIMARY KEY,
  student_id          VARCHAR(30) UNIQUE NOT NULL,
  city                VARCHAR(100),
  sponsorship_type    VARCHAR(50),
  disability_info     TEXT,
  assigned_building   VARCHAR(100),
  entry_date          DATE,
  withdrawal_date     DATE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_student_id (student_id),
  INDEX idx_assigned_building (assigned_building)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- Applications Table (Dorm applications)
-- ============================================================================
CREATE TABLE IF NOT EXISTS applications (
  id              VARCHAR(36) PRIMARY KEY,
  student_id      VARCHAR(36) NOT NULL,
  status          VARCHAR(20) NOT NULL DEFAULT 'NOT_SEEN',
  admin_note      TEXT,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_student (student_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- Announcements Table (System announcements)
-- ============================================================================
CREATE TABLE IF NOT EXISTS announcements (
  id              VARCHAR(36) PRIMARY KEY,
  title           VARCHAR(200) NOT NULL,
  body            TEXT NOT NULL,
  created_by      VARCHAR(120) NOT NULL,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- Messages Table (User-to-user messaging)
-- ============================================================================
CREATE TABLE IF NOT EXISTS messages (
  id              VARCHAR(36) PRIMARY KEY,
  from_user       VARCHAR(50) NOT NULL,
  to_user         VARCHAR(50) NOT NULL,
  content         TEXT NOT NULL,
  sent_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_from_user (from_user),
  INDEX idx_to_user (to_user),
  INDEX idx_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- Building Assignments Table (Proctor to building mapping)
-- ============================================================================
CREATE TABLE IF NOT EXISTS building_assignments (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  proctor_id      VARCHAR(36) NOT NULL,
  building_name   VARCHAR(100) NOT NULL,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (proctor_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_proctor (proctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- Document Paths Table (Store paths to uploaded documents)
-- ============================================================================
CREATE TABLE IF NOT EXISTS document_paths (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  student_id      VARCHAR(36) NOT NULL,
  file_path       VARCHAR(500) NOT NULL,
  file_type       VARCHAR(50),
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- Sample Data (for testing)
-- ============================================================================

-- Create default users
INSERT INTO users (id, username, password, role, display_name) VALUES 
('1', 'admin', 'admin123', 'ADMIN', 'Main Administrator'),
('2', 'proctor1', 'proctor123', 'PROCTOR', 'John Proctor'),
('3', 'owner', 'owner123', 'OWNER', 'System Owner'),
('4', 'student1', 'student123', 'STUDENT', 'Alice Johnson');

-- Create student profile
INSERT INTO students (user_id, student_id, city) VALUES 
('4', 'ST-1001', 'Addis Ababa');

-- Create building assignment for proctor
INSERT INTO building_assignments (proctor_id, building_name) VALUES 
('2', 'Building A');

-- Create sample announcement
INSERT INTO announcements (id, title, body, created_by) VALUES 
('ann-1', 'Welcome to Dormitory System', 'Applications are now open for the new semester. Please submit your applications before the deadline.', 'Main Administrator');
