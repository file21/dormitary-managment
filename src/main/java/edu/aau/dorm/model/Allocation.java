package edu.aau.dorm.model;

import java.time.Instant;

public final class Allocation extends BaseEntity {
    private final long applicationId;
    private final long bedId;
    private final long assignedBy;
    private final String roomNumber;
    private final Instant allocatedAt;

    public Allocation(long id, long applicationId, long bedId, long assignedBy, String roomNumber, Instant allocatedAt) {
        super(id);
        this.applicationId = applicationId;
        this.bedId = bedId;
        this.assignedBy = assignedBy;
        this.roomNumber = roomNumber;
        this.allocatedAt = allocatedAt;
    }

    public long applicationId() { return applicationId; }
    public long bedId() { return bedId; }
    public long assignedBy() { return assignedBy; }
    public String roomNumber() { return roomNumber; }
    public Instant allocatedAt() { return allocatedAt; }
}
