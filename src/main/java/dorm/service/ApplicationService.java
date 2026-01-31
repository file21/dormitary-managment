package dorm.service;

import dorm.dao.ApplicationDao;
import dorm.dao.ApplicationDaoPg;
import dorm.dao.StudentDao;
import dorm.dao.StudentDaoPg;
import dorm.model.*;

import java.time.Instant;

public final class ApplicationService {

    private final ApplicationDao applicationDao;
    private final StudentDao studentDao;
    private final NotificationService notificationService;

    public ApplicationService() {
        this(new ApplicationDaoPg(), new StudentDaoPg(), new SimpleNotificationService());
    }

    public ApplicationService(ApplicationDao applicationDao, StudentDao studentDao, NotificationService notificationService) {
        this.applicationDao = applicationDao;
        this.studentDao = studentDao;
        this.notificationService = notificationService;
    }

    public void approve(long applicationId) {
        DormApplication a = applicationDao.getById(applicationId);
        if (a.status() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING applications can be approved.");
        }
        applicationDao.setStatus(applicationId, ApplicationStatus.ACCEPTED);
        notificationService.notifyUser(a.studentUserId(), "Approved", "Your application was approved!");
    }

    public void reject(long applicationId, String reason) {
        DormApplication a = applicationDao.getById(applicationId);
        if (a.status() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING applications can be rejected.");
        }
        applicationDao.setStatus(applicationId, ApplicationStatus.REJECTED);
        notificationService.notifyUser(a.studentUserId(), "Rejected", "Reason: " + reason);
    }
}
