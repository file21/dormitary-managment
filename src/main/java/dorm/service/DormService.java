package dorm.service;

import dorm.model.ApplicationStatus;
import dorm.model.DormApplication;
import dorm.model.Role;
import dorm.model.Student;
import dorm.model.User;
import dorm.util.Validation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DormService {
    private final DormRepository repository;

    public DormService(DormRepository repository) {
        this.repository = repository;
    }

    public Optional<User> authenticate(String username, String password) {
        String safeUsername = normalizeOptional(username);
        String safePassword = normalizeOptional(password);
        if (safeUsername == null || safePassword == null || safeUsername.isBlank() || safePassword.isBlank()) {
            return Optional.empty();
        }
        return repository.findUserByUsername(safeUsername)
                .filter(user -> user.getPassword().equals(safePassword));
    }

    public Student registerStudent(String username, String password, String fullName, String studentId, String city) {
        String normalizedUsername = normalizeRequired(username, "Username");
        String normalizedPassword = normalizeRequired(password, "Password");
        String normalizedFullName = normalizeRequired(fullName, "Full name");
        String normalizedStudentId = Validation.normalizeStudentId(studentId);
        Validation.requireValidStudentId(normalizedStudentId);
        String normalizedCity = normalizeRequired(city, "City");

        if (repository.findUserByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        boolean studentIdExists = repository.getStudents().stream()
                .anyMatch(student -> student.getStudentId().equalsIgnoreCase(normalizedStudentId));
        if (studentIdExists) {
            throw new IllegalArgumentException("Student ID already registered.");
        }

        Student student = new Student(java.util.UUID.randomUUID().toString(), normalizedUsername, normalizedPassword,
                normalizedFullName, normalizedStudentId, normalizedCity);
        repository.addStudent(student);
        return student;
    }

    public DormApplication submitApplication(Student student, String sponsorshipType, String disabilityInfo) {
        Validation.require(student != null, "Student is required.");
        String normalizedSponsorship = normalizeRequired(sponsorshipType, "Sponsorship type");
        String normalizedDisability = normalizeOptional(disabilityInfo);

        student.setSponsorshipType(normalizedSponsorship);
        student.setDisabilityInfo(normalizedDisability);

        return repository.findApplicationByStudent(student)
                .orElseGet(() -> repository.createApplication(student));
    }

    public void deleteApplication(Student student) {
        repository.findApplicationByStudent(student).ifPresent(app -> {
            if (app.getStatus() == ApplicationStatus.NOT_SEEN) {
                repository.deleteApplication(app);
            }
        });
    }

    public List<DormApplication> getApplications() {
        return repository.getApplications();
    }

    public Optional<DormApplication> getApplicationForStudent(Student student) {
        return repository.findApplicationByStudent(student);
    }

    public void updateApplication(DormApplication application, ApplicationStatus status, String note) {
        Validation.require(application != null, "Application is required.");
        Validation.require(status != null, "Status is required.");
        repository.updateApplicationStatus(application, status, note);
    }

    public void assignBuilding(Student student, String buildingName) {
        Validation.require(student != null, "Student is required.");
        String normalizedBuilding = normalizeRequired(buildingName, "Building name");
        student.setAssignedBuilding(normalizedBuilding);
        student.setEntryDate(null);
        student.setWithdrawalDate(null);
        repository.findApplicationByStudent(student)
                .ifPresent(application -> repository.updateApplicationStatus(application, ApplicationStatus.ASSIGNED, application.getAdminNote()));
    }

    public void registerEntry(Student student) {
        Validation.require(student != null, "Student is required.");
        student.setEntryDate(LocalDate.now().toString());
        student.setWithdrawalDate(null);
    }

    public void registerWithdrawal(Student student) {
        Validation.require(student != null, "Student is required.");
        student.setWithdrawalDate(LocalDate.now().toString());
    }

    public List<Student> getStudents() {
        return repository.getStudents();
    }

    public List<Student> getStudentsByBuilding(String buildingName) {
        return repository.getStudentsByBuilding(buildingName);
    }

    public List<User> getUsersByRole(Role role) {
        return repository.getUsersByRole(role);
    }

    public java.util.List<User> getUsers() {
        return repository.getUsers();
    }

    public void addUser(User user) {
        Validation.require(user != null, "User is required.");
        if (repository.findUserByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        repository.addUser(user);
    }

    public void removeUser(User user) {
        Validation.require(user != null, "User is required.");
        repository.removeUser(user);
    }

    public void addAnnouncement(String title, String body, String createdBy) {
        repository.addAnnouncement(
                normalizeRequired(title, "Title"),
                normalizeRequired(body, "Body"),
                normalizeRequired(createdBy, "Creator"));
    }

    public java.util.List<dorm.model.Announcement> getAnnouncements() {
        return repository.getAnnouncements();
    }

    public void sendMessage(String fromUser, String toUser, String content) {
        repository.sendMessage(
                normalizeRequired(fromUser, "Sender"),
                normalizeRequired(toUser, "Recipient"),
                normalizeRequired(content, "Message"));
    }

    public java.util.List<dorm.model.Message> getMessagesForUser(String username) {
        return repository.getMessagesForUser(normalizeRequired(username, "Username"));
    }

    public Optional<Student> findStudentByStudentId(String studentId) {
        String normalized = Validation.normalizeStudentId(studentId);
        Validation.requireValidStudentId(normalized);
        return repository.getStudents().stream()
                .filter(student -> student.getStudentId().equalsIgnoreCase(normalized))
                .findFirst();
    }

    public java.util.List<dorm.model.BuildingAssignment> getBuildingAssignments() {
        return repository.getBuildingAssignments();
    }

    public void assignBuildingToProctor(User proctor, String buildingName) {
        Validation.require(proctor != null, "Proctor is required.");
        String normalizedBuilding = normalizeOptional(buildingName);
        if (normalizedBuilding == null || normalizedBuilding.isBlank()) {
            normalizedBuilding = "Unassigned";
        }
        repository.assignBuilding(proctor, normalizedBuilding);
    }

    private String normalizeRequired(String value, String fieldName) {
        Validation.requireNotBlank(value, fieldName);
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null ? null : value.trim();
    }
}
