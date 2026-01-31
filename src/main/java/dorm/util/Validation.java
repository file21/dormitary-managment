package dorm.util;

import java.util.regex.Pattern;

/**
 * Input validation helpers.
 */
public final class Validation {
    private Validation() {}

    // Format: ABC/1234/12 (A-Z letters, digits)
    private static final Pattern AAU_ID = Pattern.compile("^[A-Z]{3}/\\d{4}/\\d{2}$");

    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    public static boolean isValidAauId(String aauId) {
        if (aauId == null) return false;
        return AAU_ID.matcher(aauId.trim()).matches();
    }

    public static String normalizeAauId(String aauId) {
        if (aauId == null) return null;
        return aauId.trim().toUpperCase();
    }

    public static void requireValidAauId(String aauId) {
        require(isValidAauId(aauId),
                "Invalid Student ID. Expected format: ABC/1234/12 (e.g., UGR/2344/12)");
    }
}
