package dorm.service;

import dorm.model.*;

import java.util.Optional;

/**
 * In-memory service for dormitory operations (demo only).
 */
public class DormService {
    private final DormRepository repository;

    public DormService(DormRepository repository) {
        this.repository = repository;
    }

    public Optional<Object> authenticate(String username, String password) {
        Optional<User> user = repository.findUserByUsername(username);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return Optional.of(user.get());
        }

        Optional<Student> student = repository.findStudentByUsername(username);
        if (student.isPresent() && student.get().getPassword().equals(password)) {
            return Optional.of(student.get());
        }

        return Optional.empty();
    }

    public Student registerStudent(String username, String password, String fullName, String studentId, Gender gender) {
        Student student = new Student(java.util.UUID.randomUUID().toString(), username, password, fullName, studentId, gender, null);
        repository.saveStudent(student);
        return student;
    }

    public void submitApplication(Student student) {
        DormApplication application = new DormApplication(
            java.util.UUID.randomUUID().toString(),
            student
        );
        application.setStatus(ApplicationStatus.PHASE_ONE_PENDING);
        repository.saveApplication(application);
    }

    public Optional<DormApplication> getApplicationForStudent(Student student) {
        return repository.findApplicationByStudent(student);
    }

    public void updateApplicationStatus(DormApplication application, ApplicationStatus status, String note) {
        application.setStatus(status);
        application.setAdminNote(note);
    }

    public void assignBuilding(Student student, String buildingName) {
        student.setAssignedBuilding(buildingName);
    }
}
