package dorm.model;

public class DormApplication {
    private final String id;
    private final Student student;
    private ApplicationStatus status;
    private String adminNote;

    public DormApplication(String id, Student student) {
        this.id = id;
        this.student = student;
        this.status = ApplicationStatus.NOT_SEEN;
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
}
