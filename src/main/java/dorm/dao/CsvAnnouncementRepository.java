package dorm.dao;

import dorm.model.Announcement;
import dorm.util.CsvHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * CSV implementation of AnnouncementRepository.
 * Demonstrates SRP - handles only announcement CSV operations.
 */
public class CsvAnnouncementRepository implements AnnouncementRepository {
    
    private static final String FILENAME = "announcements.csv";
    private static final String HEADER = "id,title,body,created_by,created_at";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    @Override
    public List<Announcement> findAll() {
        List<String[]> records = CsvHelper.readAll(FILENAME, true);
        List<Announcement> announcements = new ArrayList<>();
        
        for (String[] record : records) {
            if (record.length >= 5) {
                announcements.add(recordToAnnouncement(record));
            }
        }
        
        // Sort by created_at descending
        announcements.sort(Comparator.comparing(Announcement::getCreatedAt).reversed());
        
        return announcements;
    }
    
    @Override
    public void save(Announcement announcement) {
        String[] record = announcementToRecord(announcement);
        CsvHelper.append(FILENAME, HEADER, record);
    }
    
    /**
     * Convert CSV record to Announcement object
     */
    private Announcement recordToAnnouncement(String[] record) {
        LocalDateTime createdAt;
        try {
            createdAt = LocalDateTime.parse(record[4], FORMATTER);
        } catch (Exception e) {
            createdAt = LocalDateTime.now();
        }
        
        return new Announcement(
            record[0],  // id
            record[1],  // title
            record[2],  // body
            record[3],  // created_by
            createdAt   // created_at
        );
    }
    
    /**
     * Convert Announcement to CSV record
     */
    private String[] announcementToRecord(Announcement announcement) {
        return new String[] {
            announcement.getId(),
            announcement.getTitle(),
            announcement.getBody(),
            announcement.getCreatedBy(),
            announcement.getCreatedAt().format(FORMATTER)
        };
    }
}
