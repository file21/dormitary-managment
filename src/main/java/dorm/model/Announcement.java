package dorm.model;

import java.time.LocalDateTime;

public class Announcement {
    private final String id;
    private String title;
    private String body;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
