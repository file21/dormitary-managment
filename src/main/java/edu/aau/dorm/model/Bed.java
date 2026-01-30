package edu.aau.dorm.model;

public final class Bed extends BaseEntity {
    private final long blockId;
    private final String bedLabel;
    private final boolean active;

    public Bed(long id, long blockId, String bedLabel, boolean active) {
        super(id);
        this.blockId = blockId;
        this.bedLabel = bedLabel;
        this.active = active;
    }

    public long blockId() { return blockId; }
    public String bedLabel() { return bedLabel; }
    public boolean active() { return active; }
}
