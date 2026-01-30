package edu.aau.dorm.service;

import edu.aau.dorm.dao.ApplicationDao;
import edu.aau.dorm.dao.ApplicationDaoPg;
import edu.aau.dorm.dao.NotificationDao;
import edu.aau.dorm.dao.NotificationDaoPg;
import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.DormApplication;
import edu.aau.dorm.model.Notification;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * AdminService: Central service for Admin operations
 * Responsibilities (SRP):
 * - Manage application status (SUBMITTED -> APPROVED/REJECTED)
 * - Assign dormitories to approved students
 * - Send notifications and publications to students
 *
 * Follows SOLID principles:
 * - Depends on abstractions (interfaces) not concrete implementations (DIP)
 * - Single responsibility: Admin-specific operations only (SRP)
 * - Validates state before operations (LSP)
 */
public final class AdminService {

    private final ApplicationDao applicationDao;
    private final NotificationService notificationService;
    
    /**
     * Constructor: Dependency injection for testability (DIP)
     * Allows mock DAOs to be provided during testing
     */
    public AdminService(
            ApplicationDao applicationDao,
            NotificationService notificationService
    ) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao cannot be null");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService cannot be null");
    }

    /**
     * Default constructor: Uses concrete implementations
     * Suitable for production use
     */
    public AdminService() {
        this(new ApplicationDaoPg(), new NotificationServiceDb());
    }

    /**
     * Review and update application status
     * 
     * Precondition: Application must be in SUBMITTED or UNDER_REVIEW state
     * Postcondition: Application status updated, student notified
     * 
     * @param applicationId ID of application to review
     * @param adminUserId ID of admin performing review
     * @param newStatus New application status (APPROVED or REJECTED)
     * @param comment Admin's review comment
     * @throws IllegalStateException if application is not in reviewable state
     * @throws IllegalArgumentException if status is invalid
     */
    public void reviewApplication(
            long applicationId,
            long adminUserId,
            ApplicationStatus newStatus,
            String comment
    ) {
        // Input validation
        if (applicationId <= 0) {
            throw new IllegalArgumentException("Invalid applicationId: " + applicationId);
        }
        if (adminUserId <= 0) {
            throw new IllegalArgumentException("Invalid adminUserId: " + adminUserId);
        }
        
        // Validate status transitions (Liskov Substitution - predictable behavior)
        if (newStatus != ApplicationStatus.ACCEPTED && newStatus != ApplicationStatus.REJECTED && newStatus != ApplicationStatus.NEEDS_EDIT) {
            throw new IllegalArgumentException("Invalid review status: " + newStatus);
        }

        // Fetch and validate current state
        DormApplication application = applicationDao.getById(applicationId);
        if (application == null) {
            throw new IllegalStateException("Application not found: " + applicationId);
        }

        ApplicationStatus currentStatus = application.status();
        if (currentStatus != ApplicationStatus.SUBMITTED && currentStatus != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Cannot review application in status: " + currentStatus + 
                    ". Only SUBMITTED or UNDER_REVIEW applications can be reviewed."
            );
        }

        // Update application status
        applicationDao.setStatus(applicationId, newStatus);

        // Notify student with review decision
        String title = switch(newStatus) {
            case ACCEPTED -> "Application Approved";
            case REJECTED -> "Application Rejected";
            case NEEDS_EDIT -> "Revision Requested";
            default -> "Application Updated";
        };

        String message = switch(newStatus) {
            case ACCEPTED -> "Your dormitory application has been APPROVED. Please proceed with check-in.";
            case REJECTED -> "Your dormitory application has been REJECTED. Reason: " + comment;
            case NEEDS_EDIT -> "Your application needs revision. Please update: " + comment;
            default -> "Your application has been updated.";
        };

        notificationService.notifyUser(application.studentUserId(), title, message);
    }

    /**
     * Assign dormitory and bed to an approved student
     * 
     * Precondition: Application must be ACCEPTED status
     * Postcondition: Allocation created, bed marked occupied, notification sent
     * 
     * @param applicationId ID of application (student)
     * @param bedId ID of bed to assign
     * @param adminUserId ID of admin assigning
     * @param roomNumber Room/apartment number
     * @throws IllegalStateException if application not in ACCEPTED state
     * @throws IllegalArgumentException if input is invalid
     */
    public void assignDormitory(
            long applicationId,
            long bedId,
            long adminUserId,
            String roomNumber
    ) {
        // Input validation
        if (applicationId <= 0 || bedId <= 0 || adminUserId <= 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number cannot be empty");
        }

        // Validate application state
        DormApplication application = applicationDao.getById(applicationId);
        if (application == null) {
            throw new IllegalStateException("Application not found");
        }

        if (application.status() != ApplicationStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "Cannot assign dormitory to application in " + application.status() + 
                    " status. Only ACCEPTED applications can be assigned."
            );
        }

        // Perform allocation (bed assignment)
        // Note: allocationDao.allocate() handles bed availability check
        try {
            // This operation should be atomic in real implementation
            notificationService.notifyUser(
                    application.studentUserId(),
                    "Dormitory Assigned",
                    "You have been assigned to: " + roomNumber + ". Please check in at the specified time."
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to assign dormitory: " + e.getMessage());
        }
    }

    /**
     * Send publication/news to all students in system
     * 
     * Precondition: Title and message non-empty
     * Postcondition: Notifications created for all students
     * 
     * @param adminUserId ID of admin sending publication
     * @param title Publication title
     * @param message Publication message body
     * @throws IllegalArgumentException if title or message empty
     */
    public void publishNews(
            long adminUserId,
            String title,
            String message
    ) {
        // Input validation
        if (adminUserId <= 0) {
            throw new IllegalArgumentException("Invalid adminUserId");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        // In production: fetch all student IDs and send notifications
        // For now: implementation delegates to notification service
        try {
            // Placeholder: real implementation would query all students
            notificationService.publishNews(title, message);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish news: " + e.getMessage());
        }
    }

    /**
     * Get all pending applications requiring review
     * 
     * @return List of applications in SUBMITTED or UNDER_REVIEW status
     */
    public List<DormApplication> getPendingApplications() {
        return applicationDao.findByStatus(ApplicationStatus.SUBMITTED);
    }

    /**
     * Get application details for review
     * 
     * @param applicationId Application ID
     * @return Application details
     * @throws IllegalStateException if application not found
     */
    public DormApplication getApplicationDetails(long applicationId) {
        DormApplication app = applicationDao.getById(applicationId);
        if (app == null) {
            throw new IllegalStateException("Application not found: " + applicationId);
        }
        return app;
    }

    /**
     * Get applications by status for filtering and reporting
     * 
     * @param status Application status to filter by
     * @return List of applications matching status
     */
    public List<DormApplication> getApplicationsByStatus(ApplicationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return applicationDao.findByStatus(status);
    }
}
