package dorm.dao;

import dorm.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Student operations.
 * Demonstrates ISP - focused interface for student-specific operations.
 */
public interface StudentRepository {
    /**
     * Find a student by student ID
     */
    Optional<Student> findByStudentId(String studentId);
    
    /**
     * Find all students
     */
    List<Student> findAll();
    
    /**
     * Find students by assigned building
     */
    List<Student> findByBuilding(String buildingName);
    
    /**
     * Save or update a student
     */
    void save(Student student);
    
    /**
     * Update student information
     */
    void update(Student student);
}
