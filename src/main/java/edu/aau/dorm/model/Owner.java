package edu.aau.dorm.model;

import java.time.Instant;

public final class Owner extends User {
    public Owner(long id, String username, String passwordHash, boolean active, Instant createdAt) {
        super(id, username, passwordHash, Role.OWNER, active, createdAt);
    }
}
