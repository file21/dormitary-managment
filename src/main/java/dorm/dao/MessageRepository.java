package dorm.dao;

import dorm.model.Message;

import java.util.List;

/**
 * Repository interface for Message operations.
 * Demonstrates ISP - focused interface for messaging operations.
 */
public interface MessageRepository {
    /**
     * Find all messages for a specific user (sent or received)
     */
    List<Message> findByUser(String username);
    
    /**
     * Save a new message
     */
    void save(Message message);
}
