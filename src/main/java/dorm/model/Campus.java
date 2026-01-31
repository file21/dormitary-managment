package dorm.model;

public final class Campus extends BaseEntity {
    private final String name;

    public Campus(long id, String name) {
        super(id);
        this.name = name;
    }

    public String name() { return name; }
}
