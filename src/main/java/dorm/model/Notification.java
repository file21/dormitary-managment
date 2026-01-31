package dorm.model;

import java.time.Instant;

public final class Notification extends BaseEntity {
    private final long toUserId;
    private final String title;
    private final String message;
    private final boolean read;
    private final Instant createdAt;

    public Notification(long id, long toUserId, String title, String message, boolean read, Instant createdAt) {
        super(id);
        this.toUserId = toUserId;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public long toUserId() { return toUserId; }
    public String title() { return title; }
    public String message() { return message; }
    public boolean read() { return read; }
    public Instant createdAt() { return createdAt; }
}
