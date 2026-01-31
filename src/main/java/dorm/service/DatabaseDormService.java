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
     * Register a new student with gender
     */
    public Student registerStudent(String username, String password, String fullName, 
                                   String studentId, String city, Gender gender) {
        Student student = new Student(
            UUID.randomUUID().toString(),
            username,
            password,
            fullName,
            studentId,
            city,
            gender
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
    
    /**
     * Update student information
     */
    public void updateStudent(Student student) {
        studentRepository.update(student);
    }
    
    // ========== Application Management ==========
    
    /**
     * Submit Phase One application (initial application)
     */
    public DormApplication submitPhaseOneApplication(Student student, SponsorshipType sponsorshipType, 
                                                      String disabilityInfo, String documentPath) {
        // Update student information
        student.setSponsorshipType(sponsorshipType);
        student.setDisabilityInfo(disabilityInfo);
        if (documentPath != null && !documentPath.isBlank()) {
            student.addDocumentPath(documentPath);
        }
        studentRepository.update(student);
        
        // Check if application already exists
        Optional<DormApplication> existing = applicationRepository.findByStudent(student);
        if (existing.isPresent()) {
            // Update existing application to pending
            DormApplication app = existing.get();
            app.setStatus(ApplicationStatus.PHASE_ONE_PENDING);
            applicationRepository.update(app);
            return app;
        }
        
        // Create new application
        DormApplication application = new DormApplication(
            UUID.randomUUID().toString(),
            student
        );
        application.setStatus(ApplicationStatus.PHASE_ONE_PENDING);
        
        applicationRepository.save(application);
        return application;
    }
    
    /**
     * Submit Phase Two application (payment slip for self-sponsored students)
     */
    public void submitPhaseTwoApplication(Student student, String paymentSlipPath) {
        student.setPaymentSlipPath(paymentSlipPath);
        studentRepository.update(student);
        
        applicationRepository.findByStudent(student).ifPresent(app -> {
            app.setStatus(ApplicationStatus.PHASE_TWO_PENDING);
            applicationRepository.update(app);
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
     * Approve Phase One application
     */
    public void approvePhaseOne(DormApplication application, String note) {
        application.setStatus(ApplicationStatus.PHASE_ONE_APPROVED);
        application.setAdminNote(note);
        applicationRepository.update(application);
    }
    
    /**
     * Decline Phase One application
     */
    public void declinePhaseOne(DormApplication application, String note) {
        application.setStatus(ApplicationStatus.PHASE_ONE_DECLINED);
        application.setAdminNote(note);
        applicationRepository.update(application);
    }
    
    /**
     * Request resubmission for Phase One
     */
    public void requestResubmit(DormApplication application, String note) {
        application.setStatus(ApplicationStatus.PHASE_ONE_RESUBMIT);
        application.setAdminNote(note);
        applicationRepository.update(application);
    }
    
    /**
     * Approve Phase Two application (payment verified)
     */
    public void approvePhaseTwoApplication(DormApplication application, String note) {
        application.setStatus(ApplicationStatus.PHASE_TWO_APPROVED);
        application.setAdminNote(note);
        applicationRepository.update(application);
    }
    
    /**
     * Decline Phase Two application (payment rejected)
     */
    public void declinePhaseTwoApplication(DormApplication application, String note) {
        application.setStatus(ApplicationStatus.PHASE_TWO_DECLINED);
        application.setAdminNote(note);
        applicationRepository.update(application);
    }
    
    /**
     * Update application status and note (generic)
     */
    public void updateApplication(DormApplication application, ApplicationStatus status, String note) {
        application.setStatus(status);
        application.setAdminNote(note);
        applicationRepository.update(application);
    }
    
    /**
     * Delete an application (only if pending)
     */
    public void deleteApplication(Student student) {
        applicationRepository.findByStudent(student).ifPresent(app -> {
            if (app.getStatus() == ApplicationStatus.PHASE_ONE_PENDING) {
                applicationRepository.delete(app);
            }
        });
    }
    
    /**
     * Check if student can fill Phase Two (self-sponsored and phase one approved)
     */
    public boolean canFillPhaseTwo(Student student) {
        if (student.getSponsorshipType() != SponsorshipType.SELF_SPONSORED) {
            return false;
        }
        return applicationRepository.findByStudent(student)
                .map(app -> app.getStatus() == ApplicationStatus.PHASE_ONE_APPROVED)
                .orElse(false);
    }
    
    /**
     * Check if student is ready for building assignment
     */
    public boolean isReadyForAssignment(DormApplication application) {
        Student student = application.getStudent();
        
        // Government students: ready after phase one approval
        if (student.getSponsorshipType() == SponsorshipType.GOVERNMENT) {
            return application.getStatus() == ApplicationStatus.PHASE_ONE_APPROVED;
        }
        
        // Self-sponsored students: ready after phase two approval
        return application.getStatus() == ApplicationStatus.PHASE_TWO_APPROVED;
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
