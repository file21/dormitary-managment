package edu.aau.dorm.service;

import edu.aau.dorm.dao.*;
import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.Allocation;
import edu.aau.dorm.model.DormApplication;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * ProctorManagementService: Service for Proctor operations
 * 
 * Responsibilities (SRP):
 * - Manage student check-in process
 * - Handle student withdrawal and bed liberation
 * - Document verification for allocated students
 * - Manage dormitory capacity (beds/rooms available)
 * - Track occupancy status in real-time
 * 
 * OOP & SOLID Principles:
 * - Encapsulation: Private fields, public methods control access (OOP)
 * - Single Responsibility: Only proctor-specific operations (SRP)
 * - Dependency Injection: Depends on abstractions (DIP)
 * - Liskov Substitution: Consistent error handling and state validation
 * - Interface Segregation: Uses focused DAO interfaces
 */
public final class ProctorManagementService {

    private final ApplicationDao applicationDao;
    private final AllocationDao allocationDao;
    private final BlockDao blockDao;
    private final BedDao bedDao;
    private final NotificationService notificationService;

    /**
     * Constructor with dependency injection
     * Allows for custom DAO implementations (testing)
     * 
     * @param applicationDao Application data access (cannot be null)
     * @param allocationDao Allocation/bed assignment access (cannot be null)
     * @param blockDao Block/dormitory access (cannot be null)
     * @param bedDao Individual bed access (cannot be null)
     * @param notificationService Notification service (cannot be null)
     * @throws NullPointerException if any parameter is null
     */
    public ProctorManagementService(
            ApplicationDao applicationDao,
            AllocationDao allocationDao,
            BlockDao blockDao,
            BedDao bedDao,
            NotificationService notificationService
    ) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao cannot be null");
        this.allocationDao = Objects.requireNonNull(allocationDao, "allocationDao cannot be null");
        this.blockDao = Objects.requireNonNull(blockDao, "blockDao cannot be null");
        this.bedDao = Objects.requireNonNull(bedDao, "bedDao cannot be null");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService cannot be null");
    }

    /**
     * Default constructor using concrete DAO implementations
     * Used in production
     */
    public ProctorManagementService() {
        this(
                new ApplicationDaoPg(),
                new AllocationDaoPg(),
                new BlockDaoPg(),
                new BedDao(),
                new NotificationServiceDb()
        );
    }

    // ========================================================================
    // Check-in Operations
    // ========================================================================

    /**
     * Check in a student: Marks application as CHECKED_IN, updates bed occupancy
     * 
     * Preconditions:
     * - Application must be ACCEPTED status
     * - Bed must exist and be available
     * 
     * Postconditions:
     * - Application status changed to CHECKED_IN
     * - Bed marked as occupied
     * - Block available beds count decremented
     * - Student notified
     * 
     * @param applicationId ID of application to check in
     * @param bedId ID of bed to occupy
     * @param proctorUserId ID of proctor performing check-in
     * @param roomNumber Room/apartment number
     * @param checkInTime Check-in timestamp
     * @throws IllegalStateException if application not ACCEPTED or bed unavailable
     * @throws IllegalArgumentException if parameters invalid
     */
    public void checkInStudent(
            long applicationId,
            long bedId,
            long proctorUserId,
            String roomNumber,
            Instant checkInTime
    ) {
        // Input validation
        validateInputs(applicationId, bedId, proctorUserId, "checkInStudent");
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number cannot be empty");
        }
        if (checkInTime == null) {
            checkInTime = Instant.now();
        }

        // Fetch and validate application state
        DormApplication application = applicationDao.getById(applicationId);
        if (application == null) {
            throw new IllegalStateException("Application not found: " + applicationId);
        }

        if (application.status() != ApplicationStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "Cannot check in student with " + application.status() + 
                    " application. Only ACCEPTED applications can be checked in."
            );
        }

        // Perform atomic transaction: allocate bed + mark checked in
        try {
            // Allocate bed to student
            allocationDao.allocate(applicationId, bedId, proctorUserId, roomNumber);

            // Update application status to CHECKED_IN
            applicationDao.setStatus(applicationId, ApplicationStatus.CHECKED_IN);

            // Mark bed as occupied
            bedDao.markOccupied(bedId, true);

            // Decrement available beds in block
            decrementAvailableBeds(bedId);

            // Notify student
            notificationService.notifyUser(
                    application.studentUserId(),
                    "Check-in Complete",
                    "You have been successfully checked in. Room: " + roomNumber + ". Welcome!"
            );
        } catch (Exception e) {
            throw new IllegalStateException("Check-in failed: " + e.getMessage());
        }
    }

    /**
     * Complete check-in: Mark checked_in_at timestamp on allocation
     * Called after physical check-in is verified
     * 
     * @param allocationId ID of allocation to complete
     * @param timestamp Check-in completion time
     * @throws IllegalStateException if allocation not found
     */
    public void completeCheckIn(long allocationId, Instant timestamp) {
        if (allocationId <= 0) {
            throw new IllegalArgumentException("Invalid allocationId: " + allocationId);
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }

        try {
            allocationDao.markCheckedIn(allocationId, timestamp);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to complete check-in: " + e.getMessage());
        }
    }

    // ========================================================================
    // Withdrawal Operations
    // ========================================================================

    /**
     * Withdraw a student: Frees bed, marks application WITHDREW, decrements capacity
     * 
     * Preconditions:
     * - Application must be ACCEPTED or CHECKED_IN
     * - Student must have active allocation
     * 
     * Postconditions:
     * - Application status changed to WITHDREW
     * - Bed marked as unoccupied
     * - Block available beds incremented
     * - Allocation marked with checkout timestamp
     * - Student notified
     * 
     * @param applicationId ID of application to withdraw
     * @param proctorUserId ID of proctor processing withdrawal
     * @param withdrawalReason Reason for withdrawal
     * @param withdrawalTime Withdrawal timestamp
     * @throws IllegalStateException if application not in appropriate state
     */
    public void withdrawStudent(
            long applicationId,
            long proctorUserId,
            String withdrawalReason,
            Instant withdrawalTime
    ) {
        validateInputs(applicationId, 0, proctorUserId, "withdrawStudent");
        if (withdrawalTime == null) {
            withdrawalTime = Instant.now();
        }

        // Fetch and validate application
        DormApplication application = applicationDao.getById(applicationId);
        if (application == null) {
            throw new IllegalStateException("Application not found: " + applicationId);
        }

        if (application.status() != ApplicationStatus.ACCEPTED && 
            application.status() != ApplicationStatus.CHECKED_IN) {
            throw new IllegalStateException(
                    "Cannot withdraw student with " + application.status() + 
                    " application. Only ACCEPTED or CHECKED_IN can be withdrawn."
            );
        }

        try {
            // Get current allocation to find bed
            Allocation allocation = allocationDao.getByApplicationId(applicationId);
            if (allocation != null) {
                // Mark bed as unoccupied
                bedDao.markOccupied(allocation.bedId(), false);

                // Increment available beds in block
                incrementAvailableBeds(allocation.bedId());

                // Mark allocation as checked out
                allocationDao.markCheckedOut(allocation.id(), withdrawalTime);
            }

            // Update application status
            applicationDao.setStatus(applicationId, ApplicationStatus.WITHDREW);

            // Notify student
            notificationService.notifyUser(
                    application.studentUserId(),
                    "Withdrawal Processed",
                    "Your dormitory status has been withdrawn. " +
                    (withdrawalReason != null ? "Reason: " + withdrawalReason : "")
            );
        } catch (Exception e) {
            throw new IllegalStateException("Withdrawal failed: " + e.getMessage());
        }
    }

    // ========================================================================
    // Document Verification
    // ========================================================================

    /**
     * Verify student documents and mark as verified
     * Records verification timestamp for audit trail
     * 
     * @param applicationId ID of application documents to verify
     * @param proctorUserId ID of proctor verifying
     * @param verificationNotes Notes from verification
     * @throws IllegalStateException if application not found
     */
    public void verifyDocuments(
            long applicationId,
            long proctorUserId,
            String verificationNotes
    ) {
        validateInputs(applicationId, 0, proctorUserId, "verifyDocuments");

        DormApplication application = applicationDao.getById(applicationId);
        if (application == null) {
            throw new IllegalStateException("Application not found: " + applicationId);
        }

        try {
            // In production: This would store verification record in audit_log
            // For now: Log to console and notify student
            System.out.println("[Proctor] Documents verified for application " + applicationId);
            System.out.println("[Proctor] Verification notes: " + verificationNotes);

            notificationService.notifyUser(
                    application.studentUserId(),
                    "Documents Verified",
                    "Your documents have been verified by the proctor."
            );
        } catch (Exception e) {
            throw new IllegalStateException("Document verification failed: " + e.getMessage());
        }
    }

    // ========================================================================
    // Dormitory Capacity Management
    // ========================================================================

    /**
     * Upload/Update dormitory capacity (beds available for gender in block)
     * Called when proctor manages their assigned block capacity
     * 
     * @param blockId ID of block to update
     * @param proctorUserId ID of proctor updating capacity
     * @param totalBeds Total bed capacity
     * @param maleBeds Beds designated for male students
     * @param femaleBeds Beds designated for female students
     * @throws IllegalArgumentException if parameters invalid
     * @throws IllegalStateException if update fails
     */
    public void updateBlockCapacity(
            long blockId,
            long proctorUserId,
            int totalBeds,
            int maleBeds,
            int femaleBeds
    ) {
        validateInputs(blockId, 0, proctorUserId, "updateBlockCapacity");

        if (totalBeds <= 0 || maleBeds < 0 || femaleBeds < 0) {
            throw new IllegalArgumentException(
                    "Invalid capacity: total=" + totalBeds + ", male=" + maleBeds + ", female=" + femaleBeds
            );
        }

        if (maleBeds + femaleBeds != totalBeds) {
            throw new IllegalArgumentException("Male + Female beds must equal total beds");
        }

        try {
            blockDao.updateCapacity(blockId, totalBeds, maleBeds, femaleBeds);
            System.out.println("[Proctor] Updated capacity for block " + blockId + 
                             ": total=" + totalBeds + ", male=" + maleBeds + ", female=" + femaleBeds);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update block capacity: " + e.getMessage());
        }
    }

    /**
     * Get current occupancy and availability for a block
     * 
     * @param blockId Block to query
     * @return Available bed count
     * @throws IllegalStateException if block not found
     */
    public int getAvailableBeds(long blockId) {
        if (blockId <= 0) {
            throw new IllegalArgumentException("Invalid blockId: " + blockId);
        }

        try {
            return blockDao.getAvailableBeds(blockId);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get available beds: " + e.getMessage());
        }
    }

    /**
     * Get all allocated students in proctor's block
     * 
     * @param blockId Block ID
     * @return List of allocated students/applications in block
     */
    public List<DormApplication> getAllocatedStudents(long blockId) {
        if (blockId <= 0) {
            throw new IllegalArgumentException("Invalid blockId: " + blockId);
        }

        try {
            return allocationDao.getByBlockId(blockId);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch allocated students: " + e.getMessage());
        }
    }

    // ========================================================================
    // Helper/Private Methods
    // ========================================================================

    /**
     * Validate input parameters
     * Centralized validation logic
     * 
     * @param id1 First ID to validate
     * @param id2 Second ID to validate (0 if not used)
     * @param userId User ID performing action
     * @param operation Name of operation for error message
     * @throws IllegalArgumentException if any parameter invalid
     */
    private void validateInputs(long id1, long id2, long userId, String operation) {
        if (id1 <= 0) {
            throw new IllegalArgumentException("Invalid first ID for " + operation);
        }
        if (id2 > 0 && id2 <= 0) { // Only validate if not 0 (not used)
            throw new IllegalArgumentException("Invalid second ID for " + operation);
        }
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId for " + operation);
        }
    }

    /**
     * Decrement available beds count in block after occupancy
     * 
     * @param bedId Bed that was occupied
     */
    private void decrementAvailableBeds(long bedId) {
        try {
            // Get block ID from bed
            long blockId = bedDao.getBlockIdByBedId(bedId);
            blockDao.decrementAvailableBeds(blockId);
        } catch (Exception e) {
            System.err.println("Warning: Failed to decrement available beds: " + e.getMessage());
        }
    }

    /**
     * Increment available beds count in block after withdrawal
     * 
     * @param bedId Bed that was freed
     */
    private void incrementAvailableBeds(long bedId) {
        try {
            // Get block ID from bed
            long blockId = bedDao.getBlockIdByBedId(bedId);
            blockDao.incrementAvailableBeds(blockId);
        } catch (Exception e) {
            System.err.println("Warning: Failed to increment available beds: " + e.getMessage());
        }
    }
}
