package edu.aau.dorm.dao;

import edu.aau.dorm.model.*;
import edu.aau.dorm.util.Db;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;

/**
 * MySQL implementation for UserDao.
 */
public final class UserDaoPg implements UserDao {
    private final DataSource ds = Db.dataSource();

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, role, active, created_at FROM app_user WHERE username = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                long id = rs.getLong("id");
                String u = rs.getString("username");
                String hash = rs.getString("password_hash");
                String role = rs.getString("role");
                boolean active = rs.getBoolean("active");
                Instant createdAt = rs.getTimestamp("created_at").toInstant();

                return Optional.of(instantiateUser(role, id, u, hash, active, createdAt));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByUsername", e);
        }
    }

    private User instantiateUser(String role, long id, String username, String hash, boolean active, Instant createdAt) {
        return switch (role) {
            case "OWNER" -> new Owner(id, username, hash, active, createdAt);
            case "ADMIN" -> new Admin(id, username, hash, active, createdAt);
            case "PROCTOR" -> new Proctor(id, username, hash, active, createdAt);
            case "STUDENT" -> new Student(id, username, hash, active, createdAt,
                    "Unknown", null, null, 1, Student.Category.NORMAL);
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }
}
