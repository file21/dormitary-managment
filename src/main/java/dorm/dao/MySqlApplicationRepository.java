package dorm.dao;

import dorm.model.ApplicationStatus;
import dorm.model.DormApplication;
import dorm.model.Student;
import dorm.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL implementation of ApplicationRepository.
 * Demonstrates SRP - focused on application data operations only.
 */
public class MySqlApplicationRepository implements ApplicationRepository {
    
    private final StudentRepository studentRepository;
    
    public MySqlApplicationRepository(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
    
    @Override
    public Optional<DormApplication> findByStudent(Student student) {
        String sql = "SELECT id, student_id, status, admin_note FROM applications WHERE student_id = ?";
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapApplication(rs, student));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding application for student: " + student.getId(), e);
        }
    }
    
    @Override
    public List<DormApplication> findAll() {
        String sql = "SELECT id, student_id, status, admin_note FROM applications";
        List<DormApplication> applications = new ArrayList<>();
        
        try (Connection conn = Db.dataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String studentId = rs.getString("student_id");
                
                // Get student from users table to find student_id
                String studentSql = "SELECT student_id FROM students WHERE user_id = ?";
                try (PreparedStatement studentStmt = conn.prepareStatement(studentSql)) {
                    studentStmt.setString(1, studentId);
                    ResultSet studentRs = studentStmt.executeQuery();
                    
                    if (studentRs.next()) {
                        String actualStudentId = studentRs.getString("student_id");
                        Optional<Student> student = studentRepository.findByStudentId(actualStudentId);
                        
                        if (student.isPresent()) {
                            applications.add(mapApplication(rs, student.get()));
                        }
                    }
                }
            }
            return applications;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all applications", e);
        }
    }
    
    @Override
    public void save(DormApplication application) {
        String sql = "INSERT INTO applications (id, student_id, status, admin_note) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, application.getId());
            stmt.setString(2, application.getStudent().getId());
            stmt.setString(3, application.getStatus().name());
            stmt.setString(4, application.getAdminNote());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error saving application: " + application.getId(), e);
        }
    }
    
    @Override
    public void update(DormApplication application) {
        String sql = "UPDATE applications SET status = ?, admin_note = ? WHERE id = ?";
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, application.getStatus().name());
            stmt.setString(2, application.getAdminNote());
            stmt.setString(3, application.getId());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error updating application: " + application.getId(), e);
        }
    }
    
    @Override
    public void delete(DormApplication application) {
        String sql = "DELETE FROM applications WHERE id = ?";
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, application.getId());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting application: " + application.getId(), e);
        }
    }
    
    /**
     * Helper method to map ResultSet to DormApplication
     */
    private DormApplication mapApplication(ResultSet rs, Student student) throws SQLException {
        DormApplication app = new DormApplication(
            rs.getString("id"),
            student
        );
        
        app.setStatus(ApplicationStatus.valueOf(rs.getString("status")));
        app.setAdminNote(rs.getString("admin_note"));
        
        return app;
    }
}
