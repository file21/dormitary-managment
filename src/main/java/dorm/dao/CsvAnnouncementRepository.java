package dorm.dao;

import dorm.model.Announcement;
import dorm.util.CsvHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        
        announcements.sort(Comparator.comparing(Announcement::getCreatedAt).reversed());
        return announcements;
    }
    
    @Override
    public void save(Announcement announcement) {
        String[] record = announcementToRecord(announcement);
        CsvHelper.append(FILENAME, HEADER, record);
    }
    
    @Override
    public void update(Announcement announcement) {
        List<Announcement> all = findAll();
        List<String[]> records = new ArrayList<>();
        
        for (Announcement a : all) {
            if (a.getId().equals(announcement.getId())) {
                records.add(announcementToRecord(announcement));
            } else {
                records.add(announcementToRecord(a));
            }
        }
        
        CsvHelper.writeAll(FILENAME, HEADER, records);
    }
    
    @Override
    public void delete(Announcement announcement) {
        List<Announcement> all = findAll();
        List<String[]> records = new ArrayList<>();
        
        for (Announcement a : all) {
            if (!a.getId().equals(announcement.getId())) {
                records.add(announcementToRecord(a));
            }
        }
        
        CsvHelper.writeAll(FILENAME, HEADER, records);
    }
    
    private Announcement recordToAnnouncement(String[] record) {
        LocalDateTime createdAt;
        try {
            createdAt = LocalDateTime.parse(record[4], FORMATTER);
        } catch (Exception e) {
            createdAt = LocalDateTime.now();
        }
        
        return new Announcement(
            record[0],
            record[1],
            record[2],
            record[3],
            createdAt
        );
    }
    
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
