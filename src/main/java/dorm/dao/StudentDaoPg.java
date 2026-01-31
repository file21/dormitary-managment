package dorm.dao;

import dorm.model.Student;
import dorm.util.Db;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;

/**
 * PostgreSQL implementation for StudentDao.
 */
public final class StudentDaoPg implements StudentDao {
    private final DataSource ds = Db.dataSource();

    @Override
    public Student getStudentByUserId(long userId) {
        String sql = "SELECT u.id, u.username, u.password_hash, u.active, u.created_at, " +
                "p.full_name, p.aau_id, p.department, p.year_of_study, p.category " +
                "FROM app_user u JOIN student_profile p ON u.id = p.user_id WHERE u.id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Student not found for user id: " + userId);

                long id = rs.getLong("id");
                String username = rs.getString("username");
                String hash = rs.getString("password_hash");
                boolean active = rs.getBoolean("active");
                Instant createdAt = rs.getTimestamp("created_at").toInstant();

                String fullName = rs.getString("full_name");
                String aauId = rs.getString("aau_id");
                String dept = rs.getString("department");
                int year = rs.getInt("year_of_study");
                Student.Category category = Student.Category.valueOf(rs.getString("category"));

                return new Student(id, username, hash, active, createdAt, fullName, aauId, dept, year, category);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error getStudentByUserId", e);
        }
    }
}
