package dorm.model;

import java.time.Instant;

public final class Owner extends User {
    public Owner(long id, String username, String password, boolean active, Instant createdAt) {
        super(id, username, password, Role.OWNER, active, createdAt);
    }
}
