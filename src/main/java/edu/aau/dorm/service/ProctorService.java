package edu.aau.dorm.service;

import edu.aau.dorm.dao.AllocationDao;
import edu.aau.dorm.dao.AllocationDaoPg;
import edu.aau.dorm.dao.ApplicationDao;
import edu.aau.dorm.dao.ApplicationDaoPg;
import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.DormApplication;

public final class ProctorService {

    private final ApplicationDao applicationDao;
    private final AllocationDao allocationDao;
    private final NotificationService notificationService;

    public ProctorService() {
        this.applicationDao = new ApplicationDaoPg();
        this.allocationDao = new AllocationDaoPg();
        this.notificationService = new SimpleNotificationService();
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
