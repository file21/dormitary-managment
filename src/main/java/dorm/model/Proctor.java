package dorm.model;

import java.time.Instant;

public final class Proctor extends User {
    public Proctor(long id, String username, String password, boolean active, Instant createdAt) {
        super(id, username, password, Role.PROCTOR, active, createdAt);
    }
}
