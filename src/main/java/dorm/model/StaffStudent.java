package dorm.model;

import java.time.Instant;

/**
 * Explicit subclass for privileged students (inheritance demo).
 */
public final class StaffStudent extends Student {
    public StaffStudent(long id, String username, String passwordHash, boolean active, Instant createdAt,
                        String fullName, String aauId, String department, int yearOfStudy) {
        super(id, username, passwordHash, active, createdAt, fullName, aauId, department, yearOfStudy, Category.STAFF_PRIVILEGED);
    }
}
