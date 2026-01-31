package dorm.model;

import java.time.Instant;

public final class Admin extends User {
    public Admin(long id, String username, String password, boolean active, Instant createdAt) {
        super(id, username, password, Role.ADMIN, active, createdAt);
    }
}
