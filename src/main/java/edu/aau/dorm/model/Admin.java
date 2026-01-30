package edu.aau.dorm.model;

import java.time.Instant;

public final class Admin extends User {
    public Admin(long id, String username, String passwordHash, boolean active, Instant createdAt) {
        super(id, username, passwordHash, Role.ADMIN, active, createdAt);
    }
}
