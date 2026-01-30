package edu.aau.dorm.model;

import java.time.Instant;

/**
 * Student is a User with student profile attributes.
 */
public class Student extends User {
    public enum Category { NORMAL, STAFF_PRIVILEGED }

    private final String fullName;
    private final String aauId;
    private final String department;
    private final int yearOfStudy;
    private final Category category;

    public Student(long id, String username, String passwordHash, boolean active, Instant createdAt,
                   String fullName, String aauId, String department, int yearOfStudy, Category category) {
        super(id, username, passwordHash, Role.STUDENT, active, createdAt);
        this.fullName = fullName;
        this.aauId = aauId;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.category = category;
    }

    public String fullName() { return fullName; }
    public String aauId() { return aauId; }
    public String department() { return department; }
    public int yearOfStudy() { return yearOfStudy; }
    public Category category() { return category; }

    public boolean isPrivileged() { return category == Category.STAFF_PRIVILEGED; }
}
