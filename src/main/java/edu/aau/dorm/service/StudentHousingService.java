package edu.aau.dorm.service;

import edu.aau.dorm.dao.*;
import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.DormApplication;
import edu.aau.dorm.model.Student;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * StudentHousingService: Service for Student-specific housing operations
 * 
 * Responsibilities (SRP):
 * - Display available dormitories and bed capacity
 * - Manage student housing applications
 * - Track student application status
 * - Provide availability and allocation information
 * 
 * OOP & SOLID Principles:
 * - Encapsulation: Private fields, controlled access (OOP)
 * - Single Responsibility: Student operations only (SRP)
 * - Dependency Injection: Interface-based dependencies (DIP)
 * - Consistent behavior: Predictable error handling (LSP)
 * - Focused interface: Only necessary methods (ISP)
 */
public final class StudentHousingService {

    private final ApplicationDao applicationDao;
    private final BlockDao blockDao;
    private final BedDao bedDao;
    private final StudentDao studentDao;
    private final NotificationService notificationService;

    /**
     * Constructor with dependency injection
     * 
     * @param applicationDao Application DAO (cannot be null)
     * @param blockDao Block DAO (cannot be null)
     * @param bedDao Bed DAO (cannot be null)
     * @param studentDao Student DAO (cannot be null)
     * @param notificationService Notification service (cannot be null)
     * @throws NullPointerException if any parameter is null
     */
    public StudentHousingService(
            ApplicationDao applicationDao,
            BlockDao blockDao,
            BedDao bedDao,
            StudentDao studentDao,
            NotificationService notificationService
    ) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao cannot be null");
        this.blockDao = Objects.requireNonNull(blockDao, "blockDao cannot be null");
        this.bedDao = Objects.requireNonNull(bedDao, "bedDao cannot be null");
        this.studentDao = Objects.requireNonNull(studentDao, "studentDao cannot be null");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService cannot be null");
    }

    /**
     * Default constructor using concrete implementations
     */
    public StudentHousingService() {
        this(
                new ApplicationDaoPg(),
                new BlockDaoPg(),
                new BedDao(),
                new StudentDaoPg(),
                new NotificationServiceDb()
        );
    }

    // ========================================================================
    // Housing Availability Queries
    // ========================================================================

    /**
     * Get all dormitory blocks with availability information
     * Used to display available housing to students
     * 
     * @return List of blocks with bed availability info
     * @throws IllegalStateException if database query fails
     */
    public List<?> getAvailableHousing() {
        try {
            // In production: Query blocks with available beds
            return blockDao.getAllWithAvailability();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch available housing: " + e.getMessage());
        }
    }

    /**
     * Get available beds in a specific block by gender
     * Students filter available beds by gender preference
     * 
     * @param blockId Block to query
     * @param gender Gender filter (MALE/FEMALE/ANY)
     * @return Count of available beds matching criteria
     * @throws IllegalArgumentException if blockId invalid
     */
    public int getAvailableBedsByGender(long blockId, String gender) {
        if (blockId <= 0) {
            throw new IllegalArgumentException("Invalid blockId: " + blockId);
        }
        if (gender == null || gender.isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be null");
        }

        try {
            // Query available beds for specified gender in block
            return bedDao.countAvailableByBlockAndGender(blockId, gender);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get available beds: " + e.getMessage());
        }
    }

    /**
     * Get occupancy percentage for a block
     * Shows how full each dormitory block is
     * 
     * @param blockId Block to query
     * @return Percentage (0-100) of beds occupied
     * @throws IllegalArgumentException if blockId invalid
     */
    public double getOccupancyPercentage(long blockId) {
        if (blockId <= 0) {
            throw new IllegalArgumentException("Invalid blockId: " + blockId);
        }

        try {
            int available = blockDao.getAvailableBeds(blockId);
            int total = blockDao.getTotalBeds(blockId);
            if (total == 0) return 0.0;
            return ((total - available) * 100.0) / total;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate occupancy: " + e.getMessage());
        }
    }

    // ========================================================================
    // Student Application Management
    // ========================================================================

    /**
     * Create new dormitory application for student
     * 
     * Precondition: Student has no existing application for window
     * Postcondition: Application created in DRAFT status
     * 
     * @param studentUserId User ID of student
     * @param windowCode Application window code
     * @param sponsorshipType GOV or SELF
     * @param campusPreference Preferred campus
     * @param hasDisability Disability indicator for priority
     * @param distance Distance from home (km)
     * @return Created application ID
     * @throws IllegalArgumentException if parameters invalid
     * @throws IllegalStateException if application exists
     */
    public long createApplication(
            long studentUserId,
            String windowCode,
            String sponsorshipType,
            String campusPreference,
            boolean hasDisability,
            double distance
    ) {
        // Input validation
        if (studentUserId <= 0) {
            throw new IllegalArgumentException("Invalid studentUserId");
        }
        if (windowCode == null || windowCode.isEmpty()) {
            throw new IllegalArgumentException("Window code required");
        }
        if (sponsorshipType == null || (!sponsorshipType.equals("GOV") && !sponsorshipType.equals("SELF"))) {
            throw new IllegalArgumentException("Invalid sponsorship type");
        }
        if (distance < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }

        // Check if student already has application for this window
        try {
            DormApplication existing = applicationDao.getByStudentAndWindow(studentUserId, windowCode);
            if (existing != null) {
                throw new IllegalStateException("Student already has application for this window");
            }

            // Create new application in DRAFT status
            long applicationId = applicationDao.create(
                    studentUserId,
                    windowCode,
                    ApplicationStatus.DRAFT,
                    sponsorshipType,
                    hasDisability,
                    campusPreference,
                    distance,
                    null // notes
            );

            notificationService.notifyUser(
                    studentUserId,
                    "Application Created",
                    "Your dormitory application has been created. You can now fill in details and submit."
            );

            return applicationId;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create application: " + e.getMessage());
        }
    }

    /**
     * Update application before submission
     * 
     * Precondition: Application in DRAFT or NEEDS_EDIT status
     * Postcondition: Application fields updated
     * 
     * @param applicationId Application to update
     * @param campusPreference Campus preference
     * @param distance Distance from home
     * @param notes Additional notes/requests
     * @throws IllegalStateException if application not updatable
     */
    public void updateApplication(
            long applicationId,
            String campusPreference,
            double distance,
            String notes
    ) {
        if (applicationId <= 0) {
            throw new IllegalArgumentException("Invalid applicationId");
        }

        try {
            DormApplication app = applicationDao.getById(applicationId);
            if (app == null) {
                throw new IllegalStateException("Application not found");
            }

            if (app.status() != ApplicationStatus.DRAFT && app.status() != ApplicationStatus.NEEDS_EDIT) {
                throw new IllegalStateException(
                        "Cannot update application in " + app.status() + " status. " +
                        "Only DRAFT or NEEDS_EDIT applications can be updated."
                );
            }

            applicationDao.update(applicationId, campusPreference, distance, notes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update application: " + e.getMessage());
        }
    }

    /**
     * Submit application for review
     * 
     * Precondition: Application in DRAFT or NEEDS_EDIT status, submission window open
     * Postcondition: Status changed to SUBMITTED, score calculated
     * 
     * @param applicationId Application to submit
     * @param studentUserId Student submitting (for verification)
     * @param submissionTime Submission timestamp
     * @throws IllegalStateException if not in submittable state
     */
    public void submitApplication(long applicationId, long studentUserId, Instant submissionTime) {
        if (applicationId <= 0 || studentUserId <= 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        if (submissionTime == null) {
            submissionTime = Instant.now();
        }

        try {
            DormApplication app = applicationDao.getById(applicationId);
            if (app == null) {
                throw new IllegalStateException("Application not found");
            }

            // Verify ownership
            if (app.studentUserId() != studentUserId) {
                throw new IllegalStateException("Cannot submit application not owned by this student");
            }

            // Check status
            if (app.status() != ApplicationStatus.DRAFT && app.status() != ApplicationStatus.NEEDS_EDIT) {
                throw new IllegalStateException("Cannot submit application in " + app.status() + " status");
            }

            // Mark as submitted
            applicationDao.markSubmitted(applicationId, 0, submissionTime); // Score calculated by admin

            notificationService.notifyUser(
                    studentUserId,
                    "Application Submitted",
                    "Your application has been submitted for review. Check notifications for updates."
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to submit application: " + e.getMessage());
        }
    }

    /**
     * Get student's current application for window
     * 
     * @param studentUserId Student ID
     * @param windowCode Window code
     * @return Application or null if not found
     */
    public DormApplication getStudentApplication(long studentUserId, String windowCode) {
        if (studentUserId <= 0) {
            throw new IllegalArgumentException("Invalid studentUserId");
        }
        if (windowCode == null || windowCode.isEmpty()) {
            throw new IllegalArgumentException("Window code required");
        }

        try {
            return applicationDao.getByStudentAndWindow(studentUserId, windowCode);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch application: " + e.getMessage());
        }
    }

    /**
     * Get application status and details
     * 
     * @param applicationId Application ID
     * @return Application details
     * @throws IllegalStateException if not found
     */
    public DormApplication getApplicationDetails(long applicationId) {
        if (applicationId <= 0) {
            throw new IllegalArgumentException("Invalid applicationId");
        }

        try {
            DormApplication app = applicationDao.getById(applicationId);
            if (app == null) {
                throw new IllegalStateException("Application not found: " + applicationId);
            }
            return app;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch application: " + e.getMessage());
        }
    }

    /**
     * Get student profile information
     * 
     * @param studentUserId Student user ID
     * @return Student profile
     * @throws IllegalStateException if not found
     */
    public Student getStudentProfile(long studentUserId) {
        if (studentUserId <= 0) {
            throw new IllegalArgumentException("Invalid studentUserId");
        }

        try {
            Student student = studentDao.getStudentByUserId(studentUserId);
            if (student == null) {
                throw new IllegalStateException("Student profile not found");
            }
            return student;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch student profile: " + e.getMessage());
        }
    }

    // ========================================================================
    // Application Status Tracking
    // ========================================================================

    /**
     * Check if student is allocated/checked in
     * 
     * @param applicationId Application ID
     * @return true if status is CHECKED_IN, false otherwise
     */
    public boolean isCheckedIn(long applicationId) {
        try {
            DormApplication app = applicationDao.getById(applicationId);
            return app != null && app.status() == ApplicationStatus.CHECKED_IN;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to check allocation status: " + e.getMessage());
        }
    }

    /**
     * Get human-readable status message for student
     * 
     * @param applicationId Application ID
     * @return Status description for display to student
     */
    public String getStatusMessage(long applicationId) {
        try {
            DormApplication app = applicationDao.getById(applicationId);
            if (app == null) {
                return "Application not found";
            }

            return switch(app.status()) {
                case DRAFT -> "Your application is saved as draft. Complete and submit it.";
                case SUBMITTED -> "Your application has been submitted for review.";
                case NEEDS_EDIT -> "Your application needs revision. Review the comments and resubmit.";
                case UNDER_REVIEW -> "Your application is under review by admin.";
                case ACCEPTED -> "Your application is ACCEPTED! Wait for check-in instructions.";
                case REJECTED -> "Your application was rejected. Please contact admin.";
                case CHECKED_IN -> "You have been checked in! Welcome to your dormitory.";
                case WITHDREW -> "Your dormitory status has been withdrawn.";
            };
        } catch (Exception e) {
            return "Unable to fetch status";
        }
    }

    /**
     * Get estimated timeline for application processing
     * 
     * @param windowCode Application window
     * @return Estimated days for processing
     */
    public int getEstimatedProcessingDays(String windowCode) {
        // Placeholder: In production, query from application_window or configuration
        return 7; // Standard 7-day processing
    }
}
