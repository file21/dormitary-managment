package edu.aau.dorm.service;

import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.DormApplication;
import edu.aau.dorm.model.Role;
import edu.aau.dorm.model.Student;
import edu.aau.dorm.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DormService {
    private final DormRepository repository;

    public DormService(DormRepository repository) {
        this.repository = repository;
    }

    public Optional<User> authenticate(String username, String password) {
        return repository.findUserByUsername(username)
                .filter(user -> user.getPassword().equals(password));
    }

    public Student registerStudent(String username, String password, String fullName, String studentId, String city) {
        Student student = new Student(java.util.UUID.randomUUID().toString(), username, password, fullName, studentId, city);
        repository.addStudent(student);
        return student;
    }

    public DormApplication submitApplication(Student student, String sponsorshipType, String disabilityInfo) {
        student.setSponsorshipType(sponsorshipType);
        student.setDisabilityInfo(disabilityInfo);
        return repository.createApplication(student);
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
        repository.updateApplicationStatus(application, status, note);
    }

    public void assignBuilding(Student student, String buildingName) {
        student.setAssignedBuilding(buildingName);
        student.setEntryDate(null);
        student.setWithdrawalDate(null);
        repository.findApplicationByStudent(student)
                .ifPresent(application -> repository.updateApplicationStatus(application, ApplicationStatus.ASSIGNED, application.getAdminNote()));
    }

    public void registerEntry(Student student) {
        student.setEntryDate(LocalDate.now().toString());
        student.setWithdrawalDate(null);
    }

    public void registerWithdrawal(Student student) {
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
        repository.addUser(user);
    }

    public void removeUser(User user) {
        repository.removeUser(user);
    }

    public void addAnnouncement(String title, String body, String createdBy) {
        repository.addAnnouncement(title, body, createdBy);
    }

    public java.util.List<edu.aau.dorm.model.Announcement> getAnnouncements() {
        return repository.getAnnouncements();
    }

    public void sendMessage(String fromUser, String toUser, String content) {
        repository.sendMessage(fromUser, toUser, content);
    }

    public java.util.List<edu.aau.dorm.model.Message> getMessagesForUser(String username) {
        return repository.getMessagesForUser(username);
    }

    public Optional<Student> findStudentByStudentId(String studentId) {
        return repository.getStudents().stream()
                .filter(student -> student.getStudentId().equalsIgnoreCase(studentId))
                .findFirst();
    }

    public java.util.List<edu.aau.dorm.model.BuildingAssignment> getBuildingAssignments() {
        return repository.getBuildingAssignments();
    }

    public void assignBuildingToProctor(User proctor, String buildingName) {
        repository.assignBuilding(proctor, buildingName);
    }
}
