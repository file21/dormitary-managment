package dorm.model;

/**
 * ENCAPSULATION DEMO: All fields are private (data hiding).
 * Only getters provided - immutable from outside.
 * This ensures data integrity and prevents invalid states.
 */
public final class Bed extends BaseEntity {
    private final long blockId;      // Which block this bed belongs to
    private final String bedLabel;   // Room number like "A-101"
    private final boolean active;    // Is this bed available?

    public Bed(long id, long blockId, String bedLabel, boolean active) {
        super(id);
        this.blockId = blockId;
        this.bedLabel = bedLabel;
        this.active = active;
    }

    // ENCAPSULATION: These methods provide controlled read-only access
    public long blockId() { return blockId; }
    public String bedLabel() { return bedLabel; }
    public boolean active() { return active; }
    
    @Override
    public String toString() {
        return "Bed[" + bedLabel + ", active=" + active + "]";
    }
}
