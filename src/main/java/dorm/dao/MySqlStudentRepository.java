package dorm.dao;

import dorm.model.Role;
import dorm.model.Student;
import dorm.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL implementation of StudentRepository.
 * Demonstrates SRP - only handles Student database operations.
 * Demonstrates DIP - implements abstraction, can be replaced with another implementation.
 */
public class MySqlStudentRepository implements StudentRepository {
    
    @Override
    public Optional<Student> findByStudentId(String studentId) {
        String sql = "SELECT u.id, u.username, u.password, u.display_name, " +
                    "s.student_id, s.city, s.sponsorship_type, s.disability_info, " +
                    "s.assigned_building, s.entry_date, s.withdrawal_date " +
                    "FROM users u JOIN students s ON u.id = s.user_id " +
                    "WHERE s.student_id = ?";
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapStudent(rs));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding student by ID: " + studentId, e);
        }
    }
    
    @Override
    public List<Student> findAll() {
        String sql = "SELECT u.id, u.username, u.password, u.display_name, " +
                    "s.student_id, s.city, s.sponsorship_type, s.disability_info, " +
                    "s.assigned_building, s.entry_date, s.withdrawal_date " +
                    "FROM users u JOIN students s ON u.id = s.user_id";
        
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = Db.dataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                students.add(mapStudent(rs));
            }
            return students;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all students", e);
        }
    }
    
    @Override
    public List<Student> findByBuilding(String buildingName) {
        String sql = "SELECT u.id, u.username, u.password, u.display_name, " +
                    "s.student_id, s.city, s.sponsorship_type, s.disability_info, " +
                    "s.assigned_building, s.entry_date, s.withdrawal_date " +
                    "FROM users u JOIN students s ON u.id = s.user_id " +
                    "WHERE s.assigned_building = ?";
        
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, buildingName);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                students.add(mapStudent(rs));
            }
            return students;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding students by building: " + buildingName, e);
        }
    }
    
    @Override
    public void save(Student student) {
        Connection conn = null;
        try {
            conn = Db.dataSource().getConnection();
            conn.setAutoCommit(false);
            
            // Insert into users table
            String userSql = "INSERT INTO users (id, username, password, role, display_name) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                stmt.setString(1, student.getId());
                stmt.setString(2, student.getUsername());
                stmt.setString(3, student.getPassword());
                stmt.setString(4, Role.STUDENT.name());
                stmt.setString(5, student.getDisplayName());
                stmt.executeUpdate();
            }
            
            // Insert into students table
            String studentSql = "INSERT INTO students (user_id, student_id, city) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(studentSql)) {
                stmt.setString(1, student.getId());
                stmt.setString(2, student.getStudentId());
                stmt.setString(3, student.getCity());
                stmt.executeUpdate();
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // ignore
                }
            }
            throw new RuntimeException("Error saving student: " + student.getUsername(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }
    
    @Override
    public void update(Student student) {
        String sql = "UPDATE students SET sponsorship_type = ?, disability_info = ?, " +
                    "assigned_building = ?, entry_date = ?, withdrawal_date = ? " +
                    "WHERE user_id = ?";
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getSponsorshipType());
            stmt.setString(2, student.getDisabilityInfo());
            stmt.setString(3, student.getAssignedBuilding());
            
            // Handle dates
            if (student.getEntryDate() != null && !student.getEntryDate().isBlank()) {
                stmt.setDate(4, Date.valueOf(student.getEntryDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            
            if (student.getWithdrawalDate() != null && !student.getWithdrawalDate().isBlank()) {
                stmt.setDate(5, Date.valueOf(student.getWithdrawalDate()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            
            stmt.setString(6, student.getId());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error updating student: " + student.getUsername(), e);
        }
    }
    
    /**
     * Helper method to map ResultSet to Student object
     */
    private Student mapStudent(ResultSet rs) throws SQLException {
        Student student = new Student(
            rs.getString("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("display_name"),
            rs.getString("student_id"),
            rs.getString("city")
        );
        
        student.setSponsorshipType(rs.getString("sponsorship_type"));
        student.setDisabilityInfo(rs.getString("disability_info"));
        student.setAssignedBuilding(rs.getString("assigned_building"));
        
        Date entryDate = rs.getDate("entry_date");
        if (entryDate != null) {
            student.setEntryDate(entryDate.toString());
        }
        
        Date withdrawalDate = rs.getDate("withdrawal_date");
        if (withdrawalDate != null) {
            student.setWithdrawalDate(withdrawalDate.toString());
        }
        
        return student;
    }
}
