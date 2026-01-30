package edu.aau.dorm.model;

public final class Block extends BaseEntity {
    private final long campusId;
    private final String blockCode;

    public Block(long id, long campusId, String blockCode) {
        super(id);
        this.campusId = campusId;
        this.blockCode = blockCode;
    }

    public long campusId() { return campusId; }
    public String blockCode() { return blockCode; }
}
