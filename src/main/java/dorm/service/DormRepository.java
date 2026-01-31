package dorm.service;

import dorm.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory repository for dormitory data (demo only).
 */
public class DormRepository {
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, DormApplication> applications = new HashMap<>();

    public DormRepository() {
        // Demo users
        users.put("admin", new User("1", "admin", "admin123", Role.ADMIN, "Admin User"));
        users.put("owner", new User("2", "owner", "owner123", Role.OWNER, "System Owner"));
        
        // Demo student
        Student student = new Student(UUID.randomUUID().toString(), "student1", "pass123", "Student One", "ST-1001", Gender.MALE, null);
        students.put(student.getStudentId(), student);
    }

    public Optional<User> findUserByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public Optional<Student> findStudentByStudentId(String studentId) {
        return Optional.ofNullable(students.get(studentId));
    }

    public Optional<Student> findStudentByUsername(String username) {
        return students.values().stream()
                .filter(s -> s.getUsername().equals(username))
                .findFirst();
    }

    public void saveStudent(Student student) {
        students.put(student.getStudentId(), student);
    }

    public void saveApplication(DormApplication application) {
        applications.put(application.getId(), application);
    }

    public Optional<DormApplication> findApplicationByStudent(Student student) {
        return applications.values().stream()
                .filter(app -> app.getStudent().equals(student))
                .findFirst();
    }
}
