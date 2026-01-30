package edu.aau.dorm.dao;

import edu.aau.dorm.util.Db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL implementation for BlockDao.
 */
public final class BlockDaoPg implements BlockDao {
    private final DataSource ds = Db.dataSource();

    @Override
    public long getAssignedBlockIdForProctor(long proctorUserId) {
        String sql = "SELECT block_id FROM proctor_assignment WHERE proctor_user_id = ? AND active = TRUE";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, proctorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("No active block assignment for proctor: " + proctorUserId);
                return rs.getLong("block_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error getAssignedBlockIdForProctor", e);
        }
    }
}
