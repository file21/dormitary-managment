package edu.aau.dorm.dao;

import edu.aau.dorm.model.*;
import edu.aau.dorm.util.Db;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation for application DAO.
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
    public List<DormApplication> findAll() {
        String sql = "SELECT id, student_user_id, window_code, sponsorship, disability, department, campus_pref, distance_km, " +
                "status, score, submitted_at, updated_at FROM dorm_application ORDER BY updated_at DESC";
        List<DormApplication> results = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("DB error findAll", e);
        }
    }

    @Override
    public DormApplication create(DormApplication application) {
        String sql = "INSERT INTO dorm_application (student_user_id, window_code, status, sponsorship, disability, department, campus_pref, " +
                "distance_km, score, submitted_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,NOW())";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, application.studentUserId());
            ps.setString(2, application.windowCode());
            ps.setString(3, application.status().name());
            ps.setString(4, application.sponsorshipType().name());
            ps.setBoolean(5, application.disability());
            ps.setString(6, application.department());
            ps.setString(7, application.campusPreference());
            if (application.distanceKm() == null) {
                ps.setNull(8, Types.DECIMAL);
            } else {
                ps.setDouble(8, application.distanceKm());
            }
            ps.setInt(9, application.score());
            if (application.submittedAt() == null) {
                ps.setNull(10, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(10, Timestamp.from(application.submittedAt()));
            }
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to get generated ID for application");
                }
                long id = keys.getLong(1);
                return new DormApplication(
                        id,
                        application.studentUserId(),
                        application.windowCode(),
                        application.sponsorshipType(),
                        application.disability(),
                        application.department(),
                        application.campusPreference(),
                        application.distanceKm(),
                        application.status(),
                        application.score(),
                        application.submittedAt(),
                        Instant.now()
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error create application", e);
        }
    }

    @Override
    public void update(DormApplication application) {
        String sql = "UPDATE dorm_application SET student_user_id=?, window_code=?, status=?, sponsorship=?, disability=?, department=?, " +
                "campus_pref=?, distance_km=?, score=?, submitted_at=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, application.studentUserId());
            ps.setString(2, application.windowCode());
            ps.setString(3, application.status().name());
            ps.setString(4, application.sponsorshipType().name());
            ps.setBoolean(5, application.disability());
            ps.setString(6, application.department());
            ps.setString(7, application.campusPreference());
            if (application.distanceKm() == null) {
                ps.setNull(8, Types.DECIMAL);
            } else {
                ps.setDouble(8, application.distanceKm());
            }
            ps.setInt(9, application.score());
            if (application.submittedAt() == null) {
                ps.setNull(10, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(10, Timestamp.from(application.submittedAt()));
            }
            ps.setLong(11, application.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error update application", e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM dorm_application WHERE id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error delete application", e);
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

    private DormApplication mapRow(ResultSet rs) throws SQLException {
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
}
