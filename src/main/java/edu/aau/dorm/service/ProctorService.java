package edu.aau.dorm.service;

import edu.aau.dorm.dao.AllocationDao;
import edu.aau.dorm.dao.AllocationDaoPg;
import edu.aau.dorm.dao.ApplicationDao;
import edu.aau.dorm.dao.ApplicationDaoPg;
import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.DormApplication;

/**
 * OOP & SOLID PRINCIPLES DEMO:
 * 
 * SINGLE RESPONSIBILITY (S in SOLID):
 * - This class ONLY handles proctor operations (check-in, withdraw)
 * - NOT handling UI, NOT handling database directly, NOT handling notifications
 * 
 * DEPENDENCY INVERSION (D in SOLID):
 * - Depends on INTERFACES: ApplicationDao, AllocationDao, NotificationService
 * - NOT on concrete implementations like ApplicationDaoPg
 * - This allows us to swap MySQL with PostgreSQL without changing this class!
 * 
 * ENCAPSULATION:
 * - All dependencies are private final fields
 * - Can't be changed from outside
 */
public final class ProctorService {

    // ENCAPSULATION: Private final fields (dependencies injected via constructor)
    private final ApplicationDao applicationDao;      // Abstract interface (not concrete class)
    private final AllocationDao allocationDao;        // Abstract interface (not concrete class)
    private final NotificationService notificationService;  // Abstract interface (not concrete class)

    /**
     * Constructor injection - DEPENDENCY INVERSION
     * Receives interfaces, not concrete implementations.
     * Makes testing easy - can pass mock objects.
     */
    public ProctorService(ApplicationDao applicationDao, AllocationDao allocationDao, NotificationService notificationService) {
        this.applicationDao = applicationDao;
        this.allocationDao = allocationDao;
        this.notificationService = notificationService;
    }

    public ProctorService() {
        this(new ApplicationDaoPg(), new AllocationDaoPg(), new NotificationServiceDb());
    }

    /**
     * CHECK-IN OPERATION
     * ENCAPSULATION DEMO: DormApplication has a status field that can only be read via getStatus()
     * The business rule is enforced here: can only check in ACCEPTED applications
     */
    public void checkIn(long applicationId, long proctorUserId, long bedId, String roomNumber) {
        // Step 1: Get application data (uses ApplicationDao interface)
        DormApplication a = applicationDao.getById(applicationId);
        
        // Step 2: Validate business rule (Encapsulation - check current status)
        if (a.status() != ApplicationStatus.ACCEPTED) {
            throw new IllegalStateException("Only ACCEPTED applications can be checked in.");
        }

        // Step 3: Update allocation (uses AllocationDao interface)
        allocationDao.allocate(applicationId, bedId, proctorUserId, roomNumber);
        
        // Step 4: Update application status (uses ApplicationDao interface)
        applicationDao.setStatus(applicationId, ApplicationStatus.CHECKED_IN);

        // Step 5: Notify student (uses NotificationService interface)
        // Notice: We don't know HOW notification works - that's abstraction!
        notificationService.notifyUser(
                a.studentUserId(),
                "Checked in",
                "You have been checked in. Room: " + roomNumber + "."
        );
    }

    public void withdraw(long applicationId, long proctorUserId, String note) {
        DormApplication a = applicationDao.getById(applicationId);
        if (a.status() != ApplicationStatus.ACCEPTED && a.status() != ApplicationStatus.CHECKED_IN) {
            throw new IllegalStateException("Only ACCEPTED/CHECKED_IN can be withdrawn.");
        }

        allocationDao.freeBedIfAllocated(applicationId);
        applicationDao.setStatus(applicationId, ApplicationStatus.WITHDREW);

        notificationService.notifyUser(
                a.studentUserId(),
                "Marked as withdrew",
                "Your dorm status was marked as withdrew. " + (note == null ? "" : note)
        );
    }
}
