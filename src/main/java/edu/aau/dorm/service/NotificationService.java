package edu.aau.dorm.service;

public interface NotificationService {
    void notifyUser(long toUserId, String title, String message);
}
