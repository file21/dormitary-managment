package dorm.dao;

import dorm.model.BuildingAssignment;
import dorm.model.Role;
import dorm.model.User;
import dorm.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL implementation of BuildingAssignmentRepository.
 * Demonstrates SRP - handles only building assignment operations.
 */
public class MySqlBuildingAssignmentRepository implements BuildingAssignmentRepository {
    
    private final UserRepository userRepository;
    
    public MySqlBuildingAssignmentRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public List<BuildingAssignment> findAll() {
        String sql = "SELECT proctor_id, building_name FROM building_assignments";
        List<BuildingAssignment> assignments = new ArrayList<>();
        
        try (Connection conn = Db.dataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String proctorId = rs.getString("proctor_id");
                
                // Find the proctor user
                String userSql = "SELECT username FROM users WHERE id = ?";
                try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                    userStmt.setString(1, proctorId);
                    ResultSet userRs = userStmt.executeQuery();
                    
                    if (userRs.next()) {
                        String username = userRs.getString("username");
                        Optional<User> proctor = userRepository.findByUsername(username);
                        
                        if (proctor.isPresent()) {
                            assignments.add(new BuildingAssignment(
                                proctor.get(),
                                rs.getString("building_name")
                            ));
                        }
                    }
                }
            }
            return assignments;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all building assignments", e);
        }
    }
    
    @Override
    public Optional<BuildingAssignment> findByProctor(User proctor) {
        String sql = "SELECT building_name FROM building_assignments WHERE proctor_id = ?";
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, proctor.getId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(new BuildingAssignment(
                    proctor,
                    rs.getString("building_name")
                ));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding assignment for proctor: " + proctor.getId(), e);
        }
    }
    
    @Override
    public void save(User proctor, String buildingName) {
        // Check if assignment exists
        Optional<BuildingAssignment> existing = findByProctor(proctor);
        
        if (existing.isPresent()) {
            // Update existing
            String sql = "UPDATE building_assignments SET building_name = ? WHERE proctor_id = ?";
            
            try (Connection conn = Db.dataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, buildingName);
                stmt.setString(2, proctor.getId());
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                throw new RuntimeException("Error updating building assignment", e);
            }
        } else {
            // Insert new
            String sql = "INSERT INTO building_assignments (proctor_id, building_name) VALUES (?, ?)";
            
            try (Connection conn = Db.dataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, proctor.getId());
                stmt.setString(2, buildingName);
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                throw new RuntimeException("Error saving building assignment", e);
            }
        }
    }
    
    @Override
    public void deleteByProctor(User proctor) {
        String sql = "DELETE FROM building_assignments WHERE proctor_id = ?";
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, proctor.getId());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting building assignment for proctor: " + proctor.getId(), e);
        }
    }
}
