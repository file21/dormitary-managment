package dorm.service;

import dorm.dao.AllocationDao;
import dorm.dao.AllocationDaoPg;
import dorm.dao.ApplicationDao;
import dorm.dao.ApplicationDaoPg;
import dorm.model.ApplicationStatus;
import dorm.model.DormApplication;

public final class ProctorService {

    private final ApplicationDao applicationDao;
    private final AllocationDao allocationDao;
    private final NotificationService notificationService;

    public ProctorService() {
        this(new ApplicationDaoPg(), new AllocationDaoPg(), new SimpleNotificationService());
    }

    public ProctorService(ApplicationDao applicationDao, AllocationDao allocationDao, NotificationService notificationService) {
        this.applicationDao = applicationDao;
        this.allocationDao = allocationDao;
        this.notificationService = notificationService;
    }

    public void checkIn(long applicationId, long proctorUserId, long bedId, String roomNumber) {
        DormApplication a = applicationDao.getById(applicationId);
        
        if (a.status() != ApplicationStatus.ACCEPTED) {
            throw new IllegalStateException("Only ACCEPTED applications can be checked in.");
        }

        allocationDao.allocate(applicationId, bedId, proctorUserId, roomNumber);
        applicationDao.setStatus(applicationId, ApplicationStatus.CHECKED_IN);
        notificationService.notifyUser(a.studentUserId(), "Checked in", "You have been checked in. Room: " + roomNumber + ".");
    }

    public void withdraw(long applicationId, long proctorUserId, String note) {
        DormApplication a = applicationDao.getById(applicationId);
        if (a.status() != ApplicationStatus.ACCEPTED && a.status() != ApplicationStatus.CHECKED_IN) {
            throw new IllegalStateException("Only ACCEPTED/CHECKED_IN can be withdrawn.");
        }

        allocationDao.freeBedIfAllocated(applicationId);
        applicationDao.setStatus(applicationId, ApplicationStatus.WITHDREW);
        notificationService.notifyUser(a.studentUserId(), "Marked as withdrew", "Your dorm status was marked as withdrew. " + (note == null ? "" : note));
    }
}
