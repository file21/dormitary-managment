package dorm.util;

import java.util.regex.Pattern;

/**
 * Input validation helpers.
 */
public final class Validation {
    private Validation() {}

    // Common AAU format: ABC/1234/12
    private static final Pattern AAU_ID = Pattern.compile("^[A-Z]{3}/\\d{4}/\\d{2}$");
    // Simple format used in sample data: ST-1001
    private static final Pattern SIMPLE_ID = Pattern.compile("^[A-Z]{2,5}-\\d{3,5}$");

    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    public static void requireNotBlank(String value, String fieldName) {
        require(value != null && !value.isBlank(), fieldName + " is required.");
    }

    public static boolean isValidStudentId(String studentId) {
        if (studentId == null) return false;
        String normalized = normalizeStudentId(studentId);
        return AAU_ID.matcher(normalized).matches() || SIMPLE_ID.matcher(normalized).matches();
    }

    public static String normalizeStudentId(String studentId) {
        if (studentId == null) return null;
        return studentId.trim().toUpperCase();
    }

    public static void requireValidStudentId(String studentId) {
        require(isValidStudentId(studentId),
                "Invalid Student ID. Expected formats: ABC/1234/12 or ST-1001.");
    }

    // Backward-compatible helpers (kept for clarity with earlier docs)
    public static boolean isValidAauId(String aauId) {
        return isValidStudentId(aauId);
    }

    public static String normalizeAauId(String aauId) {
        return normalizeStudentId(aauId);
    }

    public static void requireValidAauId(String aauId) {
        requireValidStudentId(aauId);
    }
}
