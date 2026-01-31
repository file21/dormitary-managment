package dorm.dao;

import dorm.model.DormApplication;
import dorm.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Application operations.
 * Demonstrates ISP - focused interface for application-specific operations.
 */
public interface ApplicationRepository {
    /**
     * Find application by student
     */
    Optional<DormApplication> findByStudent(Student student);
    
    /**
     * Get all applications
     */
    List<DormApplication> findAll();
    
    /**
     * Save a new application
     */
    void save(DormApplication application);
    
    /**
     * Update an application
     */
    void update(DormApplication application);
    
    /**
     * Delete an application
     */
    void delete(DormApplication application);
}
