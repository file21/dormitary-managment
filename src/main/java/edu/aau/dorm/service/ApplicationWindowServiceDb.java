package edu.aau.dorm.service;

import edu.aau.dorm.util.Db;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;

/**
 * DB-backed window checker.
 */
public final class ApplicationWindowServiceDb implements ApplicationWindowService {
    private final DataSource ds = Db.dataSource();

    @Override
    public boolean isWindowOpen(String windowCode, Instant now) {
        String sql = "SELECT open_at, close_at, active FROM application_window WHERE window_code = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, windowCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                boolean active = rs.getBoolean("active");
                Instant openAt = rs.getTimestamp("open_at").toInstant();
                Instant closeAt = rs.getTimestamp("close_at").toInstant();
                return active && !now.isBefore(openAt) && now.isBefore(closeAt);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error isWindowOpen", e);
        }
    }

    @Override
    public boolean isWindowClosed(String windowCode, Instant now) {
        String sql = "SELECT close_at, active FROM application_window WHERE window_code = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, windowCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return true;
                boolean active = rs.getBoolean("active");
                Instant closeAt = rs.getTimestamp("close_at").toInstant();
                return !active || !now.isBefore(closeAt);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error isWindowClosed", e);
        }
    }
}
