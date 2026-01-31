package dorm.model;

import java.time.Instant;

public final class Proctor extends User {
    public Proctor(long id, String username, String passwordHash, boolean active, Instant createdAt) {
        super(id, username, passwordHash, Role.PROCTOR, active, createdAt);
    }
}
