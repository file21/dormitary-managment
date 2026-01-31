package dorm.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DormApplication {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final String id;
    private final Student student;
    private ApplicationStatus status;
    private String adminNote;
    private String submittedDate;      // Date application was submitted
    private String responseHistory;    // Format: "30/12/2025_DECLINED;31/12/2025_APPROVED"

    public DormApplication(String id, Student student) {
        this.id = id;
        this.student = student;
        this.status = ApplicationStatus.PHASE_ONE_PENDING;
        this.submittedDate = LocalDate.now().format(DATE_FORMAT);
        this.responseHistory = "";
    }

    public String getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getResponseHistory() {
        return responseHistory;
    }

    public void setResponseHistory(String responseHistory) {
        this.responseHistory = responseHistory;
    }

    /**
     * Add a response entry to history
     */
    public void addResponseEntry(ApplicationStatus status) {
        String entry = LocalDate.now().format(DATE_FORMAT) + "_" + status.name();
        if (responseHistory == null || responseHistory.isEmpty()) {
            responseHistory = entry;
        } else {
            responseHistory = responseHistory + ";" + entry;
        }
    }

    /**
     * Get the latest response entry
     */
    public String getLatestResponse() {
        if (responseHistory == null || responseHistory.isEmpty()) {
            return null;
        }
        String[] entries = responseHistory.split(";");
        return entries[entries.length - 1];
    }
}
