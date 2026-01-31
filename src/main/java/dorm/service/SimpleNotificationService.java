package dorm.service;

/**
 * Simple in-memory implementation of NotificationService.
 * Just prints to console for now - good enough for learning.
 */
public class SimpleNotificationService implements NotificationService {
    
    @Override
    public void notifyUser(long toUserId, String title, String message) {
        System.out.println("[NOTIFICATION] User " + toUserId + " - " + title + ": " + message);
    }
}
