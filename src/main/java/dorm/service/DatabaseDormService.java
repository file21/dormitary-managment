package dorm.service;

import dorm.dao.*;
import dorm.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Database-backed implementation of dormitory service.
 */
public class DatabaseDormService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final AnnouncementRepository announcementRepository;
    private final MessageRepository messageRepository;
    
    public DatabaseDormService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            ApplicationRepository applicationRepository,
            AnnouncementRepository announcementRepository,
            MessageRepository messageRepository) {
        
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.announcementRepository = announcementRepository;
        this.messageRepository = messageRepository;
    }
    
    // ========== Authentication ==========
    
    public Optional<Object> authenticate(String username, String password) {
        // Check admin/owner users
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
        
        return Optional.empty();
    }
    
    // ========== Student Management ==========
    
    public Student registerStudent(String username, String password, String fullName, 
                                   String studentId, Gender gender) {
        Student student = new Student(
            UUID.randomUUID().toString(),
            username,
            password,
            fullName,
            studentId,
            gender
        );
        
        studentRepository.save(student);
        return student;
    }
    
    public List<Student> getStudents() {
        return studentRepository.findAll();
    }
    
    public Optional<Student> findStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }
    
    public void updateStudent(Student student) {
        studentRepository.update(student);
    }
    
    // ========== Application Management ==========
    
    /**
     * Submit Phase One application
     */
    public DormApplication submitPhaseOneApplication(Student student, SponsorshipType sponsorshipType,
                                                      Residency residency, String city, String subcity, 
                                                      String woreda, String disabilityInfo) {
        student.setSponsorshipType(sponsorshipType);
        student.setResidency(residency);
        student.setCity(city);
        student.setSubcity(subcity);
        student.setWoreda(woreda);
        student.setDisabilityInfo(disabilityInfo);
        studentRepository.update(student);
        
        Optional<DormApplication> existing = applicationRepository.findByStudent(student);
        if (existing.isPresent()) {
            DormApplication app = existing.get();
            app.setStatus(ApplicationStatus.PHASE_ONE_PENDING);
            applicationRepository.update(app);
            return app;
        }
        
        DormApplication application = new DormApplication(
            UUID.randomUUID().toString(),
            student
        );
        application.setStatus(ApplicationStatus.PHASE_ONE_PENDING);
        
        applicationRepository.save(application);
        return application;
    }
    
    /**
     * Submit Phase Two application (emergency contact & transaction ID)
     */
    public void submitPhaseTwoApplication(Student student, String motherName, String motherPhone,
                                          Residency motherResidency, String emergencyContact,
                                          String transactionId) {
        student.setMotherName(motherName);
        student.setMotherPhone(motherPhone);
        student.setMotherResidency(motherResidency);
        student.setEmergencyContact(emergencyContact);
        student.setTransactionId(transactionId);
        studentRepository.update(student);
        
        applicationRepository.findByStudent(student).ifPresent(app -> {
            app.setStatus(ApplicationStatus.PHASE_TWO_PENDING);
            applicationRepository.update(app);
        });
    }
    
    public Optional<DormApplication> getApplicationForStudent(Student student) {
        return applicationRepository.findByStudent(student);
    }
    
    public List<DormApplication> getApplications() {
        return applicationRepository.findAll();
    }
    
    public void approvePhaseOne(DormApplication application, String note) {
        application.setStatus(ApplicationStatus.PHASE_ONE_APPROVED);
        application.setAdminNote(note);
        applicationRepository.update(application);
    }
    
    public void declinePhaseOne(DormApplication application, String note) {
        application.setStatus(ApplicationStatus.PHASE_ONE_DECLINED);
        application.setAdminNote(note);
        applicationRepository.update(application);
    }
    
    public void requestResubmit(DormApplication application, String note) {
        application.setStatus(ApplicationStatus.PHASE_ONE_RESUBMIT);
        application.setAdminNote(note);
        applicationRepository.update(application);
    }
    
    public void approvePhaseTwoApplication(DormApplication application, String note) {
        application.setStatus(ApplicationStatus.PHASE_TWO_APPROVED);
        application.setAdminNote(note);
        applicationRepository.update(application);
    }
    
    public void declinePhaseTwoApplication(DormApplication application, String note) {
        application.setStatus(ApplicationStatus.PHASE_TWO_DECLINED);
        application.setAdminNote(note);
        applicationRepository.update(application);
    }
    
    public void updateApplication(DormApplication application) {
        applicationRepository.update(application);
    }
    
    /**
     * Check if student can fill Phase Two (phase one must be approved)
     */
    public boolean canFillPhaseTwo(Student student) {
        return applicationRepository.findByStudent(student)
                .map(app -> app.getStatus() == ApplicationStatus.PHASE_ONE_APPROVED)
                .orElse(false);
    }
    
    /**
     * Check if student is ready for building assignment (phase two approved)
     */
    public boolean isReadyForAssignment(DormApplication application) {
        return application.getStatus() == ApplicationStatus.PHASE_TWO_APPROVED;
    }
    
    /**
     * Assign a student to a building
     */
    public void assignBuilding(Student student, String buildingName) {
        student.setAssignedBuilding(buildingName);
        studentRepository.update(student);
        
        applicationRepository.findByStudent(student).ifPresent(application -> {
            application.setStatus(ApplicationStatus.ASSIGNED);
            applicationRepository.update(application);
        });
    }
    
    // ========== User Management ==========
    
    public List<User> getUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }
    
    public void addUser(User user) {
        userRepository.save(user);
    }
    
    public void removeUser(User user) {
        userRepository.delete(user);
    }
    
    // ========== Announcements ==========
    
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
    
    public List<Announcement> getAnnouncements() {
        return announcementRepository.findAll();
    }
    
    // ========== Messaging ==========
    
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
    
    public List<Message> getMessagesForUser(String username) {
        return messageRepository.findByUser(username);
    }
}
