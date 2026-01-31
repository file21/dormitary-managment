package dorm.dao;

import dorm.model.Announcement;

import java.util.List;

/**
 * Repository interface for Announcement operations.
 * Demonstrates ISP - focused interface for announcement operations.
 */
public interface AnnouncementRepository {
    /**
     * Get all announcements ordered by date
     */
    List<Announcement> findAll();
    
    /**
     * Save a new announcement
     */
    void save(Announcement announcement);
}
