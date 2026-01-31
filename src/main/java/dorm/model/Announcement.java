package dorm.model;

import java.time.LocalDateTime;

public class Announcement {
    private final String id;
    private final String title;
    private final String body;
    private final String createdBy;
    private final LocalDateTime createdAt;

    public Announcement(String id, String title, String body, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
