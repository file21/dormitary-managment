package dorm.dao;

import dorm.model.Role;
import dorm.model.User;
import dorm.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL implementation of UserRepository.
 * Demonstrates Dependency Inversion Principle - concrete class depends on abstraction.
 * Demonstrates Single Responsibility Principle - only handles User database operations.
 */
public class MySqlUserRepository implements UserRepository {
    
    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password, role, display_name FROM users WHERE username = ?";
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapUser(rs));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username: " + username, e);
        }
    }
    
    @Override
    public List<User> findByRole(Role role) {
        String sql = "SELECT id, username, password, role, display_name FROM users WHERE role = ?";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapUser(rs));
            }
            return users;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding users by role: " + role, e);
        }
    }
    
    @Override
    public List<User> findAll() {
        String sql = "SELECT id, username, password, role, display_name FROM users";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = Db.dataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapUser(rs));
            }
            return users;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all users", e);
        }
    }
    
    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (id, username, password, role, display_name) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getDisplayName());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user: " + user.getUsername(), e);
        }
    }
    
    @Override
    public void delete(User user) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getId());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + user.getUsername(), e);
        }
    }
    
    /**
     * Helper method to map ResultSet to User object
     * Demonstrates encapsulation - keeps mapping logic private
     */
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("id"),
            rs.getString("username"),
            rs.getString("password"),
            Role.valueOf(rs.getString("role")),
            rs.getString("display_name")
        );
    }
}
