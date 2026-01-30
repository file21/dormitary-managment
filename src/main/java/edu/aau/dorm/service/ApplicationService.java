package edu.aau.dorm.service;

import edu.aau.dorm.dao.ApplicationDao;
import edu.aau.dorm.dao.ApplicationDaoPg;
import edu.aau.dorm.dao.StudentDao;
import edu.aau.dorm.dao.StudentDaoPg;
import edu.aau.dorm.model.*;

import java.time.Instant;

/**
 * Central application workflow rules.
 */
public final class ApplicationService {

    private final ApplicationDao applicationDao;
    private final StudentDao studentDao;
    private final ApplicationWindowService windowService;
    private final ScoringPolicy scoringPolicy;
    private final NotificationService notificationService;

    public ApplicationService(
            ApplicationDao applicationDao,
            StudentDao studentDao,
            ApplicationWindowService windowService,
            ScoringPolicy scoringPolicy,
            NotificationService notificationService
    ) {
        this.applicationDao = applicationDao;
        this.studentDao = studentDao;
        this.windowService = windowService;
        this.scoringPolicy = scoringPolicy;
        this.notificationService = notificationService;
    }

    public ApplicationService() {
        this(new ApplicationDaoPg(), new StudentDaoPg(), new ApplicationWindowServiceDb(),
                new DefaultScoringPolicy(), new NotificationServiceDb());
    }

    public void submit(long applicationId, Instant now) {
        DormApplication a = applicationDao.getById(applicationId);
        if (!(a.status() == ApplicationStatus.DRAFT || a.status() == ApplicationStatus.NEEDS_EDIT)) {
            throw new IllegalStateException("Only DRAFT/NEEDS_EDIT applications can be submitted.");
        }
        if (!windowService.isWindowOpen(a.windowCode(), now)) {
            throw new IllegalStateException("Submission window is closed.");
        }

        Student s = studentDao.getStudentByUserId(a.studentUserId());
        int score = scoringPolicy.score(s, a);

        applicationDao.markSubmitted(applicationId, score, now);

        notificationService.notifyUser(
                a.studentUserId(),
                "Application submitted",
                "Your dorm application was submitted successfully. You will be notified after review."
        );
    }

    public void requestEdit(long applicationId, long adminUserId, String comment) {
        DormApplication a = applicationDao.getById(applicationId);
        if (a.status() != ApplicationStatus.SUBMITTED && a.status() != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Can only request edit for SUBMITTED/UNDER_REVIEW.");
        }
        applicationDao.setStatus(applicationId, ApplicationStatus.NEEDS_EDIT);

        notificationService.notifyUser(
                a.studentUserId(),
                "Correction requested",
                "Admin requested correction: " + comment
        );
    }

    public void accept(long applicationId, long adminUserId, Instant now) {
        DormApplication a = applicationDao.getById(applicationId);
        if (a.status() != ApplicationStatus.SUBMITTED && a.status() != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Can only accept SUBMITTED/UNDER_REVIEW.");
        }

        Student s = studentDao.getStudentByUserId(a.studentUserId());

        boolean fastTrack = a.disability() || s.isPrivileged();
        boolean deferred = (a.sponsorshipType() == SponsorshipType.GOV || a.sponsorshipType() == SponsorshipType.SELF);

        if (deferred && !fastTrack) {
            if (!windowService.isWindowClosed(a.windowCode(), now)) {
                throw new IllegalStateException("GOV/SELF acceptance is allowed only after submissions close.");
            }
        }

        applicationDao.setStatus(applicationId, ApplicationStatus.ACCEPTED);

        notificationService.notifyUser(
                a.studentUserId(),
                "Accepted",
                "Your dorm application has been accepted. Please wait for proctor check-in instructions."
        );
    }

    public void reject(long applicationId, long adminUserId, String reason) {
        DormApplication a = applicationDao.getById(applicationId);
        if (a.status() != ApplicationStatus.SUBMITTED && a.status() != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Can only reject SUBMITTED/UNDER_REVIEW.");
        }

        applicationDao.setStatus(applicationId, ApplicationStatus.REJECTED);

        notificationService.notifyUser(
                a.studentUserId(),
                "Rejected",
                "Your dorm application was rejected. Reason: " + reason
        );
    }
}
