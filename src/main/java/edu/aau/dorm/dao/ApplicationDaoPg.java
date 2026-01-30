package edu.aau.dorm.dao;

import edu.aau.dorm.model.*;
import edu.aau.dorm.util.Db;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;

/**
 * PostgreSQL implementation for application DAO.
 * NOTE: Some queries are provided; expand as needed for your UI.
 */
public final class ApplicationDaoPg implements ApplicationDao {
    private final DataSource ds = Db.dataSource();

    @Override
    public DormApplication getById(long id) {
        String sql = "SELECT id, student_user_id, window_code, sponsorship, disability, department, campus_pref, distance_km, " +
                "status, score, submitted_at, updated_at FROM dorm_application WHERE id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Application not found: " + id);

                long appId = rs.getLong("id");
                long studentUserId = rs.getLong("student_user_id");
                String windowCode = rs.getString("window_code");
                SponsorshipType sponsorship = SponsorshipType.valueOf(rs.getString("sponsorship"));
                boolean disability = rs.getBoolean("disability");
                String department = rs.getString("department");
                String campusPref = rs.getString("campus_pref");
                Double distance = rs.getObject("distance_km") == null ? null : rs.getDouble("distance_km");
                ApplicationStatus status = ApplicationStatus.valueOf(rs.getString("status"));
                int score = rs.getInt("score");
                Timestamp submittedAtTs = rs.getTimestamp("submitted_at");
                Timestamp updatedAtTs = rs.getTimestamp("updated_at");

                return new DormApplication(
                        appId,
                        studentUserId,
                        windowCode,
                        sponsorship,
                        disability,
                        department,
                        campusPref,
                        distance,
                        status,
                        score,
                        submittedAtTs == null ? null : submittedAtTs.toInstant(),
                        updatedAtTs == null ? Instant.now() : updatedAtTs.toInstant()
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error getById", e);
        }
    }

    @Override
    public void markSubmitted(long id, int score, Instant submittedAt) {
        String sql = "UPDATE dorm_application SET status='SUBMITTED', score=?, submitted_at=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, score);
            ps.setTimestamp(2, Timestamp.from(submittedAt));
            ps.setLong(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error markSubmitted", e);
        }
    }

    @Override
    public void setStatus(long id, ApplicationStatus status) {
        String sql = "UPDATE dorm_application SET status=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error setStatus", e);
        }
    }
}
