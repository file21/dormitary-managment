package edu.aau.dorm.ui;

import edu.aau.dorm.model.User;

/**
 * Simple session holder for the currently logged-in user.
 */
public final class SessionContext {
    private static User currentUser;

    private SessionContext() {}

    public static User getCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("No user is currently logged in.");
        }
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        currentUser = user;
    }
}
