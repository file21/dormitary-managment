package edu.aau.dorm.dao;

import edu.aau.dorm.util.Db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * MySQL implementation for notifications.
 */
public final class NotificationDaoPg implements NotificationDao {
    private final DataSource ds = Db.dataSource();

    @Override
    public void insert(long toUserId, String title, String message) {
        String sql = "INSERT INTO notification(to_user_id, title, message) VALUES(?,?,?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, toUserId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error inserting notification", e);
        }
    }
}
