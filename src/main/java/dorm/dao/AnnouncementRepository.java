package dorm.dao;

import dorm.model.Announcement;

import java.util.List;

public interface AnnouncementRepository {
    List<Announcement> findAll();
    void save(Announcement announcement);
    void update(Announcement announcement);
    void delete(Announcement announcement);
}
