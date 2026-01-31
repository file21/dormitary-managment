package dorm.dao;

public interface NotificationDao {
    void insert(long toUserId, String title, String message);
}
