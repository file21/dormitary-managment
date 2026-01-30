package edu.aau.dorm.model;

import java.time.Instant;

/**
 * Dorm application submitted by a student during an application window.
 */
public final class DormApplication extends BaseEntity {

    private final long studentUserId;
    private final String windowCode;
    private final SponsorshipType sponsorshipType;
    private final boolean disability;
    private final String department;
    private final String campusPreference;
    private final Double distanceKm;

    private final ApplicationStatus status;
    private final int score;
    private final Instant submittedAt;
    private final Instant updatedAt;

    public DormApplication(
            long id,
            long studentUserId,
            String windowCode,
            SponsorshipType sponsorshipType,
            boolean disability,
            String department,
            String campusPreference,
            Double distanceKm,
            ApplicationStatus status,
            int score,
            Instant submittedAt,
            Instant updatedAt
    ) {
        super(id);
        this.studentUserId = studentUserId;
        this.windowCode = windowCode;
        this.sponsorshipType = sponsorshipType;
        this.disability = disability;
        this.department = department;
        this.campusPreference = campusPreference;
        this.distanceKm = distanceKm;
        this.status = status;
        this.score = score;
        this.submittedAt = submittedAt;
        this.updatedAt = updatedAt;
    }

    public long studentUserId() { return studentUserId; }
    public String windowCode() { return windowCode; }
    public SponsorshipType sponsorshipType() { return sponsorshipType; }
    public boolean disability() { return disability; }
    public String department() { return department; }
    public String campusPreference() { return campusPreference; }
    public Double distanceKm() { return distanceKm; }

    public ApplicationStatus status() { return status; }
    public int score() { return score; }
    public Instant submittedAt() { return submittedAt; }
    public Instant updatedAt() { return updatedAt; }
}
