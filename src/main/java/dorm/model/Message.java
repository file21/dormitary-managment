package dorm.model;

import java.time.LocalDateTime;

public class Message {
    private final String id;
    private final String fromUser;
    private final String toUser;
    private final String content;
    private final LocalDateTime sentAt;

    public Message(String id, String fromUser, String toUser, String content, LocalDateTime sentAt) {
        this.id = id;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.content = content;
        this.sentAt = sentAt;
    }

    public String getId() {
        return id;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
