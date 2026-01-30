package edu.aau.dorm.dao;

import edu.aau.dorm.util.Db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * PostgreSQL implementation for allocation / bed assignment.
 */
public final class AllocationDaoPg implements AllocationDao {
    private final DataSource ds = Db.dataSource();

    @Override
    public void allocate(long applicationId, long bedId, long assignedByUserId, String roomNumber) {
        String sql = "INSERT INTO allocation(application_id, bed_id, assigned_by, room_number, checked_in_at) VALUES(?,?,?,?,NOW())";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            ps.setLong(2, bedId);
            ps.setLong(3, assignedByUserId);
            ps.setString(4, roomNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error allocate", e);
        }
    }

    @Override
    public void freeBedIfAllocated(long applicationId) {
        String sql = "DELETE FROM allocation WHERE application_id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error freeBedIfAllocated", e);
        }
    }
}
