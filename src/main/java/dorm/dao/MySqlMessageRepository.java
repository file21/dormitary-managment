package dorm.dao;

import dorm.model.Message;
import dorm.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation of MessageRepository.
 * Demonstrates SRP - handles only message database operations.
 */
public class MySqlMessageRepository implements MessageRepository {
    
    @Override
    public List<Message> findByUser(String username) {
        String sql = "SELECT id, from_user, to_user, content, sent_at FROM messages " +
                    "WHERE from_user = ? OR to_user = ? ORDER BY sent_at DESC";
        
        List<Message> messages = new ArrayList<>();
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                messages.add(mapMessage(rs));
            }
            return messages;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding messages for user: " + username, e);
        }
    }
    
    @Override
    public void save(Message message) {
        String sql = "INSERT INTO messages (id, from_user, to_user, content) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Db.dataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, message.getId());
            stmt.setString(2, message.getFromUser());
            stmt.setString(3, message.getToUser());
            stmt.setString(4, message.getContent());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error saving message: " + message.getId(), e);
        }
    }
    
    /**
     * Helper method to map ResultSet to Message
     */
    private Message mapMessage(ResultSet rs) throws SQLException {
        return new Message(
            rs.getString("id"),
            rs.getString("from_user"),
            rs.getString("to_user"),
            rs.getString("content"),
            rs.getTimestamp("sent_at").toLocalDateTime()
        );
    }
}
