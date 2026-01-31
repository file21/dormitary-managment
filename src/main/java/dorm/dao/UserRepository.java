package dorm.dao;

import dorm.model.Role;
import dorm.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User operations.
 * Demonstrates Interface Segregation Principle (ISP) - focused interface for user operations only.
 * Demonstrates Dependency Inversion Principle (DIP) - depend on abstraction, not concrete implementation.
 */
public interface UserRepository {
    /**
     * Find a user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find all users by role
     */
    List<User> findByRole(Role role);
    
    /**
     * Get all users
     */
    List<User> findAll();
    
    /**
     * Save a new user
     */
    void save(User user);
    
    /**
     * Delete a user
     */
    void delete(User user);
}
