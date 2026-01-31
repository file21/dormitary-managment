package dorm.service;

import dorm.dao.*;
import dorm.model.*;
import dorm.util.Validation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Database-backed implementation of dormitory service.
 * 
 * Demonstrates SOLID Principles:
 * - Single Responsibility Principle (SRP): This class only coordinates business logic
 * - Open/Closed Principle (OCP): Can extend functionality without modifying existing code
 * - Liskov Substitution Principle (LSP): Can be used wherever a service is needed
 * - Interface Segregation Principle (ISP): Uses focused DAO interfaces
 * - Dependency Inversion Principle (DIP): Depends on DAO abstractions, not concrete implementations
 */
public class DatabaseDormService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final AnnouncementRepository announcementRepository;
    private final MessageRepository messageRepository;
    private final BuildingAssignmentRepository buildingAssignmentRepository;
    
    /**
     * Constructor injection - demonstrates Dependency Inversion Principle
     * Service depends on abstractions (interfaces), not concrete classes
     */
    public DatabaseDormService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            ApplicationRepository applicationRepository,
            AnnouncementRepository announcementRepository,
            MessageRepository messageRepository,
            BuildingAssignmentRepository buildingAssignmentRepository) {
        
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.announcementRepository = announcementRepository;
        this.messageRepository = messageRepository;
        this.buildingAssignmentRepository = buildingAssignmentRepository;
    }
    
    // ========== Authentication ==========
    
    /**
     * Authenticate a user with username and password
     */
    public Optional<User> authenticate(String username, String password) {
        String safeUsername = normalizeOptional(username);
        String safePassword = normalizeOptional(password);
        if (safeUsername == null || safePassword == null || safeUsername.isBlank() || safePassword.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(safeUsername)
                .filter(user -> user.getPassword().equals(safePassword));
    }
    
    // ========== Student Management ==========
    
    /**
     * Register a new student
     */
    public Student registerStudent(String username, String password, String fullName, String studentId, String city) {
        String normalizedUsername = normalizeRequired(username, "Username");
        String normalizedPassword = normalizeRequired(password, "Password");
        String normalizedFullName = normalizeRequired(fullName, "Full name");
        String normalizedStudentId = Validation.normalizeStudentId(studentId);
        Validation.requireValidStudentId(normalizedStudentId);
        String normalizedCity = normalizeRequired(city, "City");

        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (studentRepository.findByStudentId(normalizedStudentId).isPresent()) {
            throw new IllegalArgumentException("Student ID already registered.");
        }

        Student student = new Student(
            UUID.randomUUID().toString(),
            normalizedUsername,
            normalizedPassword,
            normalizedFullName,
            normalizedStudentId,
            normalizedCity
        );
        
        studentRepository.save(student);
        return student;
    }
    
    /**
     * Get all students
     */
    public List<Student> getStudents() {
        return studentRepository.findAll();
    }
    
    /**
     * Find student by student ID
     */
    public Optional<Student> findStudentByStudentId(String studentId) {
        String normalized = Validation.normalizeStudentId(studentId);
        Validation.requireValidStudentId(normalized);
        return studentRepository.findByStudentId(normalized);
    }
    
    /**
     * Get students by assigned building
     */
    public List<Student> getStudentsByBuilding(String buildingName) {
        return studentRepository.findByBuilding(buildingName);
    }
    
    // ========== Application Management ==========
    
    /**
     * Submit a new dormitory application
     */
    public DormApplication submitApplication(Student student, String sponsorshipType, String disabilityInfo) {
        Validation.require(student != null, "Student is required.");
        String normalizedSponsorship = normalizeRequired(sponsorshipType, "Sponsorship type");
        String normalizedDisability = normalizeOptional(disabilityInfo);

        // Update student information
        student.setSponsorshipType(normalizedSponsorship);
        student.setDisabilityInfo(normalizedDisability);
        studentRepository.update(student);
        
        return applicationRepository.findByStudent(student)
                .orElseGet(() -> {
                    DormApplication application = new DormApplication(
                        UUID.randomUUID().toString(),
                        student
                    );
                    applicationRepository.save(application);
                    return application;
                });
    }
    
    /**
     * Get application for a specific student
     */
    public Optional<DormApplication> getApplicationForStudent(Student student) {
        return applicationRepository.findByStudent(student);
    }
    
    /**
     * Get all applications
     */
    public List<DormApplication> getApplications() {
        return applicationRepository.findAll();
    }
    
    /**
     * Update application status and note
     */
    public void updateApplication(DormApplication application, ApplicationStatus status, String note) {
        Validation.require(application != null, "Application is required.");
        Validation.require(status != null, "Status is required.");
        application.setStatus(status);
        application.setAdminNote(normalizeOptional(note));
        applicationRepository.update(application);
    }
    
    /**
     * Delete an application (only if not reviewed)
     */
    public void deleteApplication(Student student) {
        applicationRepository.findByStudent(student).ifPresent(app -> {
            if (app.getStatus() == ApplicationStatus.NOT_SEEN) {
                applicationRepository.delete(app);
            }
        });
    }
    
    // ========== Building Assignment ==========
    
    /**
     * Assign a student to a building
     */
    public void assignBuilding(Student student, String buildingName) {
        Validation.require(student != null, "Student is required.");
        String normalizedBuilding = normalizeRequired(buildingName, "Building name");

        DormApplication application = applicationRepository.findByStudent(student)
                .orElseThrow(() -> new IllegalStateException("Student has no application on record."));
        if (application.getStatus() != ApplicationStatus.APPROVED && application.getStatus() != ApplicationStatus.ASSIGNED) {
            throw new IllegalStateException("Only approved applications can be assigned.");
        }

        student.setAssignedBuilding(normalizedBuilding);
        student.setEntryDate(null);
        student.setWithdrawalDate(null);
        studentRepository.update(student);

        // Update application status to ASSIGNED
        application.setStatus(ApplicationStatus.ASSIGNED);
        applicationRepository.update(application);
    }
    
    /**
     * Register student entry to dormitory
     */
    public void registerEntry(Student student) {
        Validation.require(student != null, "Student is required.");
        student.setEntryDate(LocalDate.now().toString());
        student.setWithdrawalDate(null);
        studentRepository.update(student);
    }
    
    /**
     * Register student withdrawal from dormitory
     */
    public void registerWithdrawal(Student student) {
        Validation.require(student != null, "Student is required.");
        student.setWithdrawalDate(LocalDate.now().toString());
        studentRepository.update(student);
    }
    
    // ========== Proctor Building Assignment ==========
    
    /**
     * Get all building assignments
     */
    public List<BuildingAssignment> getBuildingAssignments() {
        return buildingAssignmentRepository.findAll();
    }
    
    /**
     * Assign a building to a proctor
     */
    public void assignBuildingToProctor(User proctor, String buildingName) {
        Validation.require(proctor != null, "Proctor is required.");
        String normalizedBuilding = normalizeOptional(buildingName);
        if (normalizedBuilding == null || normalizedBuilding.isBlank()) {
            normalizedBuilding = "Unassigned";
        }
        buildingAssignmentRepository.save(proctor, normalizedBuilding);
    }
    
    // ========== User Management ==========
    
    /**
     * Get all users
     */
    public List<User> getUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Get users by role
     */
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Add a new user (admin or proctor)
     */
    public void addUser(User user) {
        Validation.require(user != null, "User is required.");
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        userRepository.save(user);
    }
    
    /**
     * Remove a user
     */
    public void removeUser(User user) {
        Validation.require(user != null, "User is required.");
        // Remove building assignment if proctor
        if (user.getRole() == Role.PROCTOR) {
            buildingAssignmentRepository.deleteByProctor(user);
        }
        
        userRepository.delete(user);
    }
    
    // ========== Announcements ==========
    
    /**
     * Add a new announcement
     */
    public void addAnnouncement(String title, String body, String createdBy) {
        String normalizedTitle = normalizeRequired(title, "Title");
        String normalizedBody = normalizeRequired(body, "Body");
        String normalizedCreator = normalizeRequired(createdBy, "Creator");
        Announcement announcement = new Announcement(
            UUID.randomUUID().toString(),
            normalizedTitle,
            normalizedBody,
            normalizedCreator,
            LocalDateTime.now()
        );
        
        announcementRepository.save(announcement);
    }
    
    /**
     * Get all announcements
     */
    public List<Announcement> getAnnouncements() {
        return announcementRepository.findAll();
    }
    
    // ========== Messaging ==========
    
    /**
     * Send a message
     */
    public void sendMessage(String fromUser, String toUser, String content) {
        String normalizedFrom = normalizeRequired(fromUser, "Sender");
        String normalizedTo = normalizeRequired(toUser, "Recipient");
        String normalizedContent = normalizeRequired(content, "Message");
        Message message = new Message(
            UUID.randomUUID().toString(),
            normalizedFrom,
            normalizedTo,
            normalizedContent,
            LocalDateTime.now()
        );
        
        messageRepository.save(message);
    }
    
    /**
     * Get messages for a user
     */
    public List<Message> getMessagesForUser(String username) {
        String normalizedUsername = normalizeRequired(username, "Username");
        return messageRepository.findByUser(normalizedUsername);
    }

    private String normalizeRequired(String value, String fieldName) {
        Validation.requireNotBlank(value, fieldName);
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null ? null : value.trim();
    }
}
