package edu.aau.dorm.service;

import edu.aau.dorm.dao.NotificationDao;
import edu.aau.dorm.dao.NotificationDaoPg;

/**
 * Simple in-app notification service.
 */
public final class NotificationServiceDb implements NotificationService {
    private final NotificationDao notificationDao = new NotificationDaoPg();

    @Override
    public void notifyUser(long toUserId, String title, String message) {
        notificationDao.insert(toUserId, title, message);
    }
}
