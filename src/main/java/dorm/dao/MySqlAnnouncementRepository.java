package dorm.dao;

import dorm.model.Announcement;
import dorm.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of AnnouncementRepository.
 * Demonstrates SRP - handles only announcement database operations.
 */
public class MySqlAnnouncementRepository implements AnnouncementRepository {
    
    @Override
    public List<Announcement> findAll() {
        String sql = "SELECT id, title, body, created_by, created_at FROM announcements ORDER BY created_at DESC";
        List<Announcement> announcements = new ArrayList<>();
        
        try (Connection conn = Db.dataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                announcements.add(mapAnnouncement(rs));
            }
            return announcements;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all announcements", e);
        }
    }
    
    @Override
    public void save(Announcement announcement) {
        String sql = "INSERT INTO announcements (id, title, body, created_by) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, announcement.getId());
            stmt.setString(2, announcement.getTitle());
            stmt.setString(3, announcement.getBody());
            stmt.setString(4, announcement.getCreatedBy());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error saving announcement: " + announcement.getTitle(), e);
        }
    }
    
    /**
     * Helper method to map ResultSet to Announcement
     */
    private Announcement mapAnnouncement(ResultSet rs) throws SQLException {
        return new Announcement(
            rs.getString("id"),
            rs.getString("title"),
            rs.getString("body"),
            rs.getString("created_by"),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
