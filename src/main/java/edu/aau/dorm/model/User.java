package edu.aau.dorm.model;

import java.time.Instant;

/**
 * Base user for authentication and role checks.
 */
public abstract class User extends BaseEntity {
    public enum Role { OWNER, ADMIN, PROCTOR, STUDENT }

    private final String username;
    private final String passwordHash;
    private final Role role;
    private final boolean active;
    private final Instant createdAt;

    protected User(long id, String username, String passwordHash, Role role, boolean active, Instant createdAt) {
        super(id);
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public String username() { return username; }
    public String passwordHash() { return passwordHash; }
    public Role role() { return role; }
    public boolean active() { return active; }
    public Instant createdAt() { return createdAt; }

    public boolean canManageUsers() {
        return role == Role.OWNER;
    }
}
