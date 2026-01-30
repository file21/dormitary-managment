package edu.aau.dorm.model;

import java.time.Instant;

/**
 * ENCAPSULATION DEMO: All fields are private and final.
 * Data can only be read via getter methods - no setter methods.
 * This is immutable encapsulation - very safe!
 * 
 * Demonstrates: Encapsulation (private fields with getters only)
 */
public final class DormApplication extends BaseEntity {

    // ENCAPSULATION: Private fields - hidden from outside world
    private final long studentUserId;        // Which student submitted
    private final String windowCode;         // Application window ID
    private final SponsorshipType sponsorshipType;  // Scholarship type
    private final boolean disability;        // Has disability?
    private final String department;         // Student's department
    private final String campusPreference;   // Preferred campus
    private final Double distanceKm;         // Distance from campus

    // Status fields - show where in process
    private final ApplicationStatus status;  // Current application status
    private final int score;                 // Priority score
    private final Instant submittedAt;       // When submitted
    private final Instant updatedAt;         // Last updated

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
