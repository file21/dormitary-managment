package edu.aau.dorm.service;

import edu.aau.dorm.model.Announcement;
import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.BuildingAssignment;
import edu.aau.dorm.model.DormApplication;
import edu.aau.dorm.model.Message;
import edu.aau.dorm.model.Role;
import edu.aau.dorm.model.Student;
import edu.aau.dorm.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class DormRepository {
    private final List<User> users = new ArrayList<>();
    private final List<Student> students = new ArrayList<>();
    private final List<DormApplication> applications = new ArrayList<>();
    private final List<Announcement> announcements = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();
    private final List<BuildingAssignment> buildingAssignments = new ArrayList<>();

    public DormRepository() {
        seed();
    }

    private void seed() {
        User admin = new User(UUID.randomUUID().toString(), "admin", "admin123", Role.ADMIN, "Main Admin");
        User proctor = new User(UUID.randomUUID().toString(), "proctor1", "pass123", Role.PROCTOR, "Proctor One");
        User owner = new User(UUID.randomUUID().toString(), "owner", "owner123", Role.OWNER, "System Owner");
        Student student = new Student(UUID.randomUUID().toString(), "student1", "pass123", "Student One", "ST-1001", "Addis Ababa");

        users.add(admin);
        users.add(proctor);
        users.add(owner);
        users.add(student);
        students.add(student);

        buildingAssignments.add(new BuildingAssignment(proctor, "Building A"));

        announcements.add(new Announcement(UUID.randomUUID().toString(), "Dorm Opening", "Applications are now open.", admin.getDisplayName(), LocalDateTime.now()));
    }

    public Optional<User> findUserByUsername(String username) {
        return users.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public List<User> getUsersByRole(Role role) {
        return users.stream()
                .filter(user -> user.getRole() == role)
                .collect(Collectors.toList());
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void addStudent(Student student) {
        students.add(student);
        users.add(student);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public List<DormApplication> getApplications() {
        return applications;
    }

    public Optional<DormApplication> findApplicationByStudent(Student student) {
        return applications.stream()
                .filter(app -> app.getStudent().equals(student))
                .findFirst();
    }

    public DormApplication createApplication(Student student) {
        DormApplication application = new DormApplication(UUID.randomUUID().toString(), student);
        applications.add(application);
        return application;
    }

    public void deleteApplication(DormApplication application) {
        applications.remove(application);
    }

    public List<Announcement> getAnnouncements() {
        return announcements.stream()
                .sorted(Comparator.comparing(Announcement::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public void addAnnouncement(String title, String body, String createdBy) {
        announcements.add(new Announcement(UUID.randomUUID().toString(), title, body, createdBy, LocalDateTime.now()));
    }

    public List<Message> getMessagesForUser(String username) {
        return messages.stream()
                .filter(message -> message.getToUser().equalsIgnoreCase(username) || message.getFromUser().equalsIgnoreCase(username))
                .sorted(Comparator.comparing(Message::getSentAt).reversed())
                .collect(Collectors.toList());
    }

    public void sendMessage(String fromUser, String toUser, String content) {
        messages.add(new Message(UUID.randomUUID().toString(), fromUser, toUser, content, LocalDateTime.now()));
    }

    public List<BuildingAssignment> getBuildingAssignments() {
        return buildingAssignments;
    }

    public Optional<BuildingAssignment> findBuildingAssignment(User proctor) {
        return buildingAssignments.stream()
                .filter(assignment -> assignment.getProctor().equals(proctor))
                .findFirst();
    }

    public void assignBuilding(User proctor, String buildingName) {
        Optional<BuildingAssignment> existing = findBuildingAssignment(proctor);
        if (existing.isPresent()) {
            existing.get().setBuildingName(buildingName);
        } else {
            buildingAssignments.add(new BuildingAssignment(proctor, buildingName));
        }
    }

    public void removeUser(User user) {
        users.remove(user);
        if (user instanceof Student student) {
            students.remove(student);
            findApplicationByStudent(student).ifPresent(applications::remove);
        }
        buildingAssignments.removeIf(assignment -> assignment.getProctor().equals(user));
    }

    public List<Student> getStudentsByBuilding(String buildingName) {
        return students.stream()
                .filter(student -> buildingName.equalsIgnoreCase(student.getAssignedBuilding()))
                .collect(Collectors.toList());
    }

    public void updateApplicationStatus(DormApplication application, ApplicationStatus status, String note) {
        application.setStatus(status);
        application.setAdminNote(note);
    }
}
