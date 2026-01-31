package dorm.service;

import dorm.dao.*;
import dorm.model.*;

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
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password));
    }
    
    // ========== Student Management ==========
    
    /**
     * Register a new student
     */
    public Student registerStudent(String username, String password, String fullName, String studentId, String city) {
        Student student = new Student(
            UUID.randomUUID().toString(),
            username,
            password,
            fullName,
            studentId,
            city
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
        return studentRepository.findByStudentId(studentId);
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
        // Update student information
        student.setSponsorshipType(sponsorshipType);
        student.setDisabilityInfo(disabilityInfo);
        studentRepository.update(student);
        
        // Create application
        DormApplication application = new DormApplication(
            UUID.randomUUID().toString(),
            student
        );
        
        applicationRepository.save(application);
        return application;
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
        application.setStatus(status);
        application.setAdminNote(note);
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
        student.setAssignedBuilding(buildingName);
        student.setEntryDate(null);
        student.setWithdrawalDate(null);
        studentRepository.update(student);
        
        // Update application status to ASSIGNED
        applicationRepository.findByStudent(student).ifPresent(application -> {
            application.setStatus(ApplicationStatus.ASSIGNED);
            applicationRepository.update(application);
        });
    }
    
    /**
     * Register student entry to dormitory
     */
    public void registerEntry(Student student) {
        student.setEntryDate(LocalDate.now().toString());
        student.setWithdrawalDate(null);
        studentRepository.update(student);
    }
    
    /**
     * Register student withdrawal from dormitory
     */
    public void registerWithdrawal(Student student) {
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
        buildingAssignmentRepository.save(proctor, buildingName);
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
        userRepository.save(user);
    }
    
    /**
     * Remove a user
     */
    public void removeUser(User user) {
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
        Announcement announcement = new Announcement(
            UUID.randomUUID().toString(),
            title,
            body,
            createdBy,
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
        Message message = new Message(
            UUID.randomUUID().toString(),
            fromUser,
            toUser,
            content,
            LocalDateTime.now()
        );
        
        messageRepository.save(message);
    }
    
    /**
     * Get messages for a user
     */
    public List<Message> getMessagesForUser(String username) {
        return messageRepository.findByUser(username);
    }
}
