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
    private final BuildingRepository buildingRepository;
    
    /**
     * Constructor injection - demonstrates Dependency Inversion Principle
     */
    public DatabaseDormService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            ApplicationRepository applicationRepository,
            AnnouncementRepository announcementRepository,
            MessageRepository messageRepository,
            BuildingRepository buildingRepository) {
        
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.announcementRepository = announcementRepository;
        this.messageRepository = messageRepository;
        this.buildingRepository = buildingRepository;
    }
    
    // ========== Authentication ==========
    
    /**
     * Authenticate a user - checks users, students, and buildings
     * Returns Object that can be User, Student, or Building
     */
    public Optional<Object> authenticate(String username, String password) {
        // Check admin/owner users first
        Optional<User> user = userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password));
        if (user.isPresent()) {
            return Optional.of(user.get());
        }
        
        // Check students
        for (Student student : studentRepository.findAll()) {
            if (student.getUsername().equals(username) && student.getPassword().equals(password)) {
                return Optional.of(student);
            }
        }
        
        // Check buildings (proctor login)
        Optional<Building> building = buildingRepository.findByName(username)
                .filter(b -> b.getPassword().equals(password));
        if (building.isPresent()) {
            return Optional.of(building.get());
        }
        
        return Optional.empty();
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
    
    // ========== Building Management ==========
    
    /**
     * Get all buildings
     */
    public List<Building> getBuildings() {
        return buildingRepository.findAll();
    }
    
    /**
     * Find building by name
     */
    public Optional<Building> findBuildingByName(String name) {
        return buildingRepository.findByName(name);
    }
    
    /**
     * Add a new building (proctor account)
     */
    public Building addBuilding(String name, String password, int maxCapacity) {
        Building building = new Building(
            UUID.randomUUID().toString(),
            name,
            password,
            maxCapacity
        );
        buildingRepository.save(building);
        return building;
    }
    
    /**
     * Update building (e.g., change capacity)
     */
    public void updateBuilding(Building building) {
        buildingRepository.update(building);
    }
    
    /**
     * Delete a building
     */
    public void deleteBuilding(Building building) {
        buildingRepository.delete(building);
    }
    
    /**
     * Get current occupancy of a building (students with entry date and no withdrawal)
     */
    public int getBuildingOccupancy(String buildingName) {
        int count = 0;
        for (Student student : studentRepository.findByBuilding(buildingName)) {
            // Count only students who have entered and not withdrawn
            if (student.getEntryDate() != null && !student.getEntryDate().isBlank()) {
                if (student.getWithdrawalDate() == null || student.getWithdrawalDate().isBlank()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Get remaining capacity of a building
     */
    public int getBuildingRemainingCapacity(String buildingName) {
        Optional<Building> building = buildingRepository.findByName(buildingName);
        if (building.isEmpty()) {
            return 0;
        }
        return building.get().getMaxCapacity() - getBuildingOccupancy(buildingName);
    }
    
    /**
     * Check if building has available space
     */
    public boolean hasBuildingCapacity(String buildingName) {
        return getBuildingRemainingCapacity(buildingName) > 0;
    }
    
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
     * Register student entry to dormitory (affects occupancy)
     */
    public void registerEntry(Student student) {
        student.setEntryDate(LocalDate.now().toString());
        student.setWithdrawalDate(null);
        studentRepository.update(student);
    }
    
    /**
     * Register student withdrawal from dormitory (frees up capacity)
     */
    public void registerWithdrawal(Student student) {
        student.setWithdrawalDate(LocalDate.now().toString());
        studentRepository.update(student);
    }
    
    // ========== User Management (Admin/Owner only) ==========
    
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
     * Add a new user (admin only - proctors are now buildings)
     */
    public void addUser(User user) {
        userRepository.save(user);
    }
    
    /**
     * Remove a user
     */
    public void removeUser(User user) {
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
