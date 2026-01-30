package edu.aau.dorm.service;

import edu.aau.dorm.dao.ApplicationDao;
import edu.aau.dorm.dao.ApplicationDaoPg;
import edu.aau.dorm.model.*;
import edu.aau.dorm.util.Validation;

import java.time.Instant;
import java.util.List;

public final class ApplicationService {

    private final ApplicationDao applicationDao;
    private final NotificationService notificationService;

    public ApplicationService() {
        this.applicationDao = new ApplicationDaoPg();
        this.notificationService = new SimpleNotificationService();
    }

    public List<DormApplication> listAll() {
        return applicationDao.findAll();
    }

    public DormApplication create(DormApplication application) {
        validate(application);
        return applicationDao.create(application);
    }

    public void update(DormApplication application) {
        Validation.require(application.id() > 0, "Select an application to update.");
        validate(application);
        applicationDao.update(application);
    }

    public void delete(long applicationId) {
        Validation.require(applicationId > 0, "Select an application to delete.");
        applicationDao.delete(applicationId);
    }

    public void approve(long applicationId) {
        DormApplication a = applicationDao.getById(applicationId);
        if (!isReviewable(a.status())) {
            throw new IllegalStateException("Only SUBMITTED or UNDER_REVIEW applications can be approved.");
        }
        applicationDao.setStatus(applicationId, ApplicationStatus.ACCEPTED);
        notificationService.notifyUser(a.studentUserId(), "Approved", "Your application was approved!");
    }

    public void reject(long applicationId, String reason) {
        DormApplication a = applicationDao.getById(applicationId);
        if (!isReviewable(a.status())) {
            throw new IllegalStateException("Only SUBMITTED or UNDER_REVIEW applications can be rejected.");
        }
        applicationDao.setStatus(applicationId, ApplicationStatus.REJECTED);
        notificationService.notifyUser(a.studentUserId(), "Rejected", "Reason: " + reason);
    }

    private boolean isReviewable(ApplicationStatus status) {
        return status == ApplicationStatus.SUBMITTED || status == ApplicationStatus.UNDER_REVIEW;
    }

    private void validate(DormApplication application) {
        Validation.require(application.studentUserId() > 0, "Student user ID is required.");
        Validation.require(application.windowCode() != null && !application.windowCode().isBlank(),
                "Application window is required.");
        Validation.require(application.sponsorshipType() != null, "Sponsorship is required.");
        Validation.require(application.status() != null, "Status is required.");
        Validation.require(application.score() >= 0, "Score must be zero or positive.");
        if (application.distanceKm() != null) {
            Validation.require(application.distanceKm() >= 0, "Distance must be zero or positive.");
        }
    }
}
