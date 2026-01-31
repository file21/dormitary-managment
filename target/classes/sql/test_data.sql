-- Test Data for Dormitory Management System (MySQL)
-- Author: AAU Dorm Management Team
-- This file contains sample data for testing the system

USE dormdb;

-- ============================================================================
-- Clear existing data (use with caution - for testing only)
-- ============================================================================

-- TRUNCATE TABLE audit_log;
-- TRUNCATE TABLE notification;
-- TRUNCATE TABLE allocation;
-- TRUNCATE TABLE application_review;
-- TRUNCATE TABLE dorm_application;
-- TRUNCATE TABLE application_window;
-- TRUNCATE TABLE proctor_assignment;
-- TRUNCATE TABLE bed;
-- TRUNCATE TABLE block;
-- TRUNCATE TABLE student_profile;
-- TRUNCATE TABLE app_user;
-- TRUNCATE TABLE campus;

-- ============================================================================
-- Insert Campuses
-- ============================================================================

INSERT IGNORE INTO campus (id, name, location) VALUES 
(1, 'Main Campus', 'Addis Ababa - Kokebe Tsibaye'),
(2, 'North Campus', 'Adama City'),
(3, 'South Campus', 'Awassa City');

-- ============================================================================
-- Insert Blocks (Dormitories)
-- ============================================================================

INSERT IGNORE INTO block (id, campus_id, block_code, gender, total_beds, available_beds) VALUES 
-- Main Campus - Male Blocks
(1, 1, 'B1-MALE', 'MALE', 60, 45),
(2, 1, 'B2-MALE', 'MALE', 55, 38),

-- Main Campus - Female Blocks
(3, 1, 'B1-FEMALE', 'FEMALE', 50, 28),
(4, 1, 'B2-FEMALE', 'FEMALE', 48, 35),

-- North Campus - Male Blocks
(5, 2, 'B3-MALE', 'MALE', 70, 52),

-- North Campus - Female Blocks
(6, 2, 'B3-FEMALE', 'FEMALE', 60, 40),

-- South Campus - Mixed
(7, 3, 'B4-MALE', 'MALE', 40, 25),
(8, 3, 'B4-FEMALE', 'FEMALE', 40, 18);

-- ============================================================================
-- Insert Beds
-- ============================================================================

-- Block 1 (B1-MALE) - 60 beds total, occupancy: 15
INSERT IGNORE INTO bed (id, block_id, bed_label, is_occupied, is_active) VALUES 
(1, 1, 'R01-B1', FALSE, TRUE),
(2, 1, 'R01-B2', TRUE, TRUE),
(3, 1, 'R01-B3', FALSE, TRUE),
(4, 1, 'R01-B4', TRUE, TRUE),
(5, 1, 'R01-B5', FALSE, TRUE),
(6, 1, 'R02-B1', FALSE, TRUE),
(7, 1, 'R02-B2', TRUE, TRUE),
(8, 1, 'R02-B3', FALSE, TRUE),
(9, 1, 'R02-B4', TRUE, TRUE),
(10, 1, 'R02-B5', FALSE, TRUE),
(11, 1, 'R03-B1', TRUE, TRUE),
(12, 1, 'R03-B2', FALSE, TRUE),
(13, 1, 'R03-B3', TRUE, TRUE),
(14, 1, 'R03-B4', FALSE, TRUE),
(15, 1, 'R03-B5', TRUE, TRUE);

-- Block 3 (B1-FEMALE) - 50 beds total, occupancy: 22
INSERT IGNORE INTO bed (id, block_id, bed_label, is_occupied, is_active) VALUES 
(51, 3, 'R01-G1', FALSE, TRUE),
(52, 3, 'R01-G2', TRUE, TRUE),
(53, 3, 'R01-G3', FALSE, TRUE),
(54, 3, 'R01-G4', TRUE, TRUE),
(55, 3, 'R01-G5', TRUE, TRUE),
(56, 3, 'R02-G1', FALSE, TRUE),
(57, 3, 'R02-G2', TRUE, TRUE),
(58, 3, 'R02-G3', FALSE, TRUE),
(59, 3, 'R02-G4', TRUE, TRUE),
(60, 3, 'R02-G5', TRUE, TRUE);

-- ============================================================================
-- Insert Users
-- ============================================================================

-- Admin user (password: admin123)
INSERT IGNORE INTO app_user (id, username, password, role, active) VALUES 
(1, 'admin1', 'admin123', 'ADMIN', TRUE),
(2, 'admin2', 'admin123', 'ADMIN', TRUE);

-- Proctor users (assigned to blocks)
INSERT IGNORE INTO app_user (id, username, password, role, active) VALUES 
(3, 'proctor_main_male', 'proctor123', 'PROCTOR', TRUE),
(4, 'proctor_main_female', 'proctor123', 'PROCTOR', TRUE),
(5, 'proctor_north', 'proctor123', 'PROCTOR', TRUE),
(6, 'proctor_south', 'proctor123', 'PROCTOR', TRUE);

-- Student users (various statuses)
INSERT IGNORE INTO app_user (id, username, password, role, active) VALUES 
(10, 'student001', 'student123', 'STUDENT', TRUE),
(11, 'student002', 'student123', 'STUDENT', TRUE),
(12, 'student003', 'student123', 'STUDENT', TRUE),
(13, 'student004', 'student123', 'STUDENT', TRUE),
(14, 'student005', 'student123', 'STUDENT', TRUE),
(15, 'student006', 'student123', 'STUDENT', TRUE);

-- ============================================================================
-- Insert Student Profiles
-- ============================================================================

INSERT IGNORE INTO student_profile (user_id, full_name, aau_id, department, year_of_study, category, gender) VALUES 
(10, 'Abebe Kebede', 'AAU001', 'Computer Science', 2, 'NORMAL', 'MALE'),
(11, 'Selamawit Tesfaye', 'AAU002', 'Biology', 3, 'NORMAL', 'FEMALE'),
(12, 'Yohannes Tadesse', 'AAU003', 'Engineering', 1, 'STAFF_PRIVILEGED', 'MALE'),
(13, 'Almaz Getnet', 'AAU004', 'Medicine', 4, 'NORMAL', 'FEMALE'),
(14, 'Dawit Abraham', 'AAU005', 'Law', 2, 'NORMAL', 'MALE'),
(15, 'Meaza Solomon', 'AAU006', 'Education', 3, 'STAFF_PRIVILEGED', 'FEMALE');

-- ============================================================================
-- Insert Proctor Assignments
-- ============================================================================

INSERT IGNORE INTO proctor_assignment (proctor_user_id, block_id, active) VALUES 
(3, 1, TRUE),  -- Proctor Main Male -> Block B1-MALE
(3, 2, TRUE),  -- Proctor Main Male -> Block B2-MALE
(4, 3, TRUE),  -- Proctor Main Female -> Block B1-FEMALE
(4, 4, TRUE),  -- Proctor Main Female -> Block B2-FEMALE
(5, 5, TRUE),  -- Proctor North -> Block B3-MALE
(5, 6, TRUE),  -- Proctor North -> Block B3-FEMALE
(6, 7, TRUE),  -- Proctor South -> Block B4-MALE
(6, 8, TRUE);  -- Proctor South -> Block B4-FEMALE

-- ============================================================================
-- Insert Application Windows
-- ============================================================================

INSERT IGNORE INTO application_window (window_code, open_at, close_at, active) VALUES 
('2024-MAIN', '2024-01-01 00:00:00', '2024-01-31 23:59:59', TRUE),
('2024-RESIT', '2024-06-01 00:00:00', '2024-06-30 23:59:59', FALSE),
('2025-MAIN', '2025-01-01 00:00:00', '2025-01-31 23:59:59', FALSE);

-- ============================================================================
-- Insert Dormitory Applications
-- ============================================================================

-- Student 1 (Abebe) - DRAFT
INSERT IGNORE INTO dorm_application (id, student_user_id, window_code, status, sponsorship, disability, department, campus_pref, distance_km, notes, score, submitted_at, updated_at) VALUES 
(1, 10, '2024-MAIN', 'DRAFT', 'GOV', FALSE, 'Computer Science', 'Main Campus', 15.5, 'Prefer Block B1', 0, NULL, NOW());

-- Student 2 (Selamawit) - SUBMITTED
INSERT IGNORE INTO dorm_application (id, student_user_id, window_code, status, sponsorship, disability, department, campus_pref, distance_km, notes, score, submitted_at, updated_at) VALUES 
(2, 11, '2024-MAIN', 'SUBMITTED', 'GOV', FALSE, 'Biology', 'Main Campus', 25.0, 'Far from home, need accommodation', 85, '2024-01-15 10:30:00', NOW());

-- Student 3 (Yohannes) - ACCEPTED (privileged/disabled priority)
INSERT IGNORE INTO dorm_application (id, student_user_id, window_code, status, sponsorship, disability, department, campus_pref, distance_km, notes, score, submitted_at, updated_at) VALUES 
(3, 12, '2024-MAIN', 'ACCEPTED', 'GOV', TRUE, 'Engineering', 'Main Campus', 5.0, 'Disability: Mobility restricted', 95, '2024-01-10 08:00:00', NOW());

-- Student 4 (Almaz) - CHECKED_IN
INSERT IGNORE INTO dorm_application (id, student_user_id, window_code, status, sponsorship, disability, department, campus_pref, distance_km, notes, score, submitted_at, updated_at) VALUES 
(4, 13, '2024-MAIN', 'CHECKED_IN', 'SELF', FALSE, 'Medicine', 'Main Campus', 18.0, 'Self-sponsored student', 88, '2024-01-12 09:15:00', NOW());

-- Student 5 (Dawit) - REJECTED
INSERT IGNORE INTO dorm_application (id, student_user_id, window_code, status, sponsorship, disability, department, campus_pref, distance_km, notes, score, submitted_at, updated_at) VALUES 
(5, 14, '2024-MAIN', 'REJECTED', 'SELF', FALSE, 'Law', 'North Campus', 50.0, 'Out of range', 45, '2024-01-14 14:20:00', NOW());

-- Student 6 (Meaza) - UNDER_REVIEW
INSERT IGNORE INTO dorm_application (id, student_user_id, window_code, status, sponsorship, disability, department, campus_pref, distance_km, notes, score, submitted_at, updated_at) VALUES 
(6, 15, '2024-MAIN', 'UNDER_REVIEW', 'GOV', FALSE, 'Education', 'South Campus', 200.0, 'Very far, needs accommodation', 80, '2024-01-13 11:45:00', NOW());

-- ============================================================================
-- Insert Allocations
-- ============================================================================

-- Yohannes (Student 3) allocated to Block 1, Room R01-B2
INSERT IGNORE INTO allocation (id, application_id, bed_id, assigned_by, room_number, allocated_at, checked_in_at, checked_out_at) VALUES 
(1, 3, 2, 1, 'R01-B2', '2024-01-20 10:00:00', '2024-01-20 14:00:00', NULL);

-- Almaz (Student 4) allocated to Block 3, Room R01-G2
INSERT IGNORE INTO allocation (id, application_id, bed_id, assigned_by, room_number, allocated_at, checked_in_at, checked_out_at) VALUES 
(2, 4, 52, 1, 'R01-G2', '2024-01-18 09:30:00', '2024-01-18 15:00:00', NULL);

-- ============================================================================
-- Insert Application Reviews
-- ============================================================================

-- Review of Selamawit's application
INSERT IGNORE INTO application_review (id, application_id, reviewer_user_id, decision, comment, created_at) VALUES 
(1, 2, 1, 'ACCEPTED', 'Eligible for accommodation. Approved for check-in.', '2024-01-16 10:00:00');

-- Review of Yohannes's application
INSERT IGNORE INTO application_review (id, application_id, reviewer_user_id, decision, comment, created_at) VALUES 
(2, 3, 1, 'ACCEPTED', 'Staff privileged + Disabled. High priority. Approved.', '2024-01-17 11:30:00');

-- Review of Almaz's application
INSERT IGNORE INTO application_review (id, application_id, reviewer_user_id, decision, comment, created_at) VALUES 
(3, 4, 2, 'ACCEPTED', 'Self-sponsored but eligible. Approved for accommodation.', '2024-01-18 09:00:00');

-- Review of Dawit's application (Rejected)
INSERT IGNORE INTO application_review (id, application_id, reviewer_user_id, decision, comment, created_at) VALUES 
(4, 5, 1, 'REJECTED', 'Distance exceeds maximum criteria. Out of range for accommodation.', '2024-01-15 14:00:00');

-- ============================================================================
-- Insert Notifications
-- ============================================================================

-- Notification for Abebe (application created)
INSERT IGNORE INTO notification (id, to_user_id, title, message, is_read, created_at) VALUES 
(1, 10, 'Application Created', 'Your dormitory application has been created. Complete the form and submit.', FALSE, NOW());

-- Notification for Selamawit (application submitted)
INSERT IGNORE INTO notification (id, to_user_id, title, message, is_read, created_at) VALUES 
(2, 11, 'Application Submitted', 'Your application has been submitted for review. You will be notified of the outcome.', TRUE, '2024-01-15 10:35:00');

-- Notification for Selamawit (approved)
INSERT IGNORE INTO notification (id, to_user_id, title, message, is_read, created_at) VALUES 
(3, 11, 'Application Approved', 'Your dormitory application has been APPROVED. Wait for check-in instructions.', FALSE, '2024-01-16 10:05:00');

-- Notification for Yohannes (approved)
INSERT IGNORE INTO notification (id, to_user_id, title, message, is_read, created_at) VALUES 
(4, 12, 'Application Approved', 'Your dormitory application has been APPROVED (Staff/Disability Priority).', FALSE, '2024-01-17 11:35:00');

-- Notification for Yohannes (checked in)
INSERT IGNORE INTO notification (id, to_user_id, title, message, is_read, created_at) VALUES 
(5, 12, 'Check-in Complete', 'You have been checked in to Room R01-B2. Welcome to your dormitory!', FALSE, '2024-01-20 14:05:00');

-- Notification for Almaz (approved)
INSERT IGNORE INTO notification (id, to_user_id, title, message, is_read, created_at) VALUES 
(6, 13, 'Application Approved', 'Your dormitory application has been APPROVED. Proceed with check-in.', TRUE, '2024-01-18 09:05:00');

-- Notification for Almaz (checked in)
INSERT IGNORE INTO notification (id, to_user_id, title, message, is_read, created_at) VALUES 
(7, 13, 'Check-in Complete', 'You have been checked in to Room R01-G2. Welcome!', FALSE, '2024-01-18 15:05:00');

-- Notification for Dawit (rejected)
INSERT IGNORE INTO notification (id, to_user_id, title, message, is_read, created_at) VALUES 
(8, 14, 'Application Rejected', 'Your dormitory application has been REJECTED. Distance exceeds criteria.', FALSE, '2024-01-15 14:05:00');

-- ============================================================================
-- Insert Audit Logs
-- ============================================================================

INSERT IGNORE INTO audit_log (id, actor_user_id, action, entity_type, entity_id, details, created_at) VALUES 
(1, 1, 'CREATE_APPLICATION', 'DormApplication', 1, 'Application created for student 10', '2024-01-14 08:00:00'),
(2, 1, 'SUBMIT_APPLICATION', 'DormApplication', 2, 'Application submitted by student 11', '2024-01-15 10:30:00'),
(3, 1, 'APPROVE_APPLICATION', 'DormApplication', 2, 'Application approved by admin', '2024-01-16 10:00:00'),
(4, 1, 'ALLOCATE_BED', 'Allocation', 1, 'Student 12 allocated to bed 2 in block 1', '2024-01-20 10:00:00'),
(5, 3, 'CHECK_IN_STUDENT', 'Allocation', 1, 'Student checked in by proctor', '2024-01-20 14:00:00');

-- ============================================================================
-- Verify Data Insertion
-- ============================================================================

-- Show summary statistics
SELECT 'DORMITORY MANAGEMENT SYSTEM - TEST DATA SUMMARY' AS Status;
SELECT CONCAT('Total Users: ', COUNT(*)) FROM app_user;
SELECT CONCAT('Total Students: ', COUNT(*)) FROM student_profile;
SELECT CONCAT('Total Applications: ', COUNT(*)) FROM dorm_application;
SELECT CONCAT('Total Allocations: ', COUNT(*)) FROM allocation;
SELECT CONCAT('Total Notifications: ', COUNT(*)) FROM notification;

-- Show application status breakdown
SELECT 'Application Status Breakdown:' AS Status;
SELECT status, COUNT(*) as count FROM dorm_application GROUP BY status;

-- Show blocks with occupancy
SELECT 'Block Occupancy Summary:' AS Status;
SELECT 
    b.block_code,
    c.name as campus,
    b.gender,
    b.total_beds,
    (b.total_beds - b.available_beds) as occupied,
    b.available_beds,
    CONCAT(ROUND(((b.total_beds - b.available_beds) * 100.0 / b.total_beds), 1), '%') as occupancy
FROM block b
JOIN campus c ON b.campus_id = c.id
ORDER BY b.campus_id, b.block_code;
