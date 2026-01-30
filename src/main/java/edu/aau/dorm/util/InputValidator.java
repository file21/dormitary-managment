package edu.aau.dorm.util;

import java.util.regex.Pattern;

/**
 * InputValidator: Centralized input validation utility
 * 
 * Responsibilities (SRP):
 * - Validate all user inputs across the application
 * - Provide consistent error messages
 * - Prevent invalid data from entering business logic
 * 
 * OOP Principles:
 * - Encapsulation: All validation logic centralized
 * - Single Responsibility: Only validation concerns
 * - Reusability: Used across all layers
 */
public final class InputValidator {

    // Regex patterns
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{9,15}$");
    private static final Pattern AAU_ID_PATTERN = Pattern.compile("^[A-Z]{2,3}\\d{4,6}$");

    private InputValidator() {
        // Utility class: prevent instantiation
    }

    // ========================================================================
    // String Validations
    // ========================================================================

    /**
     * Validate non-empty string
     * 
     * @param value String to validate
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if string is null or empty
     */
    public static void validateNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    /**
     * Validate string length
     * 
     * @param value String to validate
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if length out of range
     */
    public static void validateLength(String value, int minLength, int maxLength, String fieldName) {
        validateNonEmpty(value, fieldName);
        if (value.length() < minLength || value.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + minLength + " and " + maxLength + " characters. " +
                    "Got: " + value.length()
            );
        }
    }

    /**
     * Validate username format
     * 
     * @param username Username to validate
     * @throws IllegalArgumentException if format invalid
     */
    public static void validateUsername(String username) {
        validateNonEmpty(username, "Username");
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Username must be 3-50 characters (alphanumeric, dots, dashes, underscores)"
            );
        }
    }

    /**
     * Validate password strength
     * 
     * @param password Password to validate
     * @throws IllegalArgumentException if password too weak
     */
    public static void validatePassword(String password) {
        validateNonEmpty(password, "Password");
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }

    /**
     * Validate email address
     * 
     * @param email Email to validate
     * @throws IllegalArgumentException if email format invalid
     */
    public static void validateEmail(String email) {
        validateNonEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        if (email.length() > 100) {
            throw new IllegalArgumentException("Email too long (max 100 characters)");
        }
    }

    /**
     * Validate phone number
     * 
     * @param phone Phone number to validate
     * @throws IllegalArgumentException if format invalid
     */
    public static void validatePhoneNumber(String phone) {
        validateNonEmpty(phone, "Phone number");
        if (!PHONE_PATTERN.matcher(phone.replaceAll("[^0-9]", "")).matches()) {
            throw new IllegalArgumentException("Phone number must be 9-15 digits");
        }
    }

    /**
     * Validate AAU ID format
     * 
     * @param aauId AAU ID to validate
     * @throws IllegalArgumentException if format invalid
     */
    public static void validateAauId(String aauId) {
        validateNonEmpty(aauId, "AAU ID");
        if (!AAU_ID_PATTERN.matcher(aauId).matches()) {
            throw new IllegalArgumentException("Invalid AAU ID format. Expected: 2-3 letters + 4-6 digits");
        }
    }

    // ========================================================================
    // Numeric Validations
    // ========================================================================

    /**
     * Validate positive integer
     * 
     * @param value Value to validate
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if value not positive
     */
    public static void validatePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive. Got: " + value);
        }
    }

    /**
     * Validate non-negative integer
     * 
     * @param value Value to validate
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if value negative
     */
    public static void validateNonNegative(long value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative. Got: " + value);
        }
    }

    /**
     * Validate positive double
     * 
     * @param value Value to validate
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if value not positive
     */
    public static void validatePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive. Got: " + value);
        }
    }

    /**
     * Validate value in range
     * 
     * @param value Value to validate
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if value out of range
     */
    public static void validateRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + min + " and " + max + ". Got: " + value
            );
        }
    }

    /**
     * Validate range for double
     * 
     * @param value Value to validate
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if value out of range
     */
    public static void validateRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + min + " and " + max + ". Got: " + value
            );
        }
    }

    // ========================================================================
    // Enum Validations
    // ========================================================================

    /**
     * Validate enum value
     * 
     * @param value Value to check
     * @param validValues Valid enum values
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if value not valid
     */
    public static <T extends Enum<T>> void validateEnumValue(T value, T[] validValues, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        
        for (T validValue : validValues) {
            if (value.equals(validValue)) {
                return;
            }
        }
        
        throw new IllegalArgumentException(
                fieldName + " has invalid value: " + value
        );
    }

    /**
     * Validate gender value
     * 
     * @param gender Gender to validate (MALE, FEMALE)
     * @throws IllegalArgumentException if invalid
     */
    public static void validateGender(String gender) {
        validateNonEmpty(gender, "Gender");
        if (!gender.equals("MALE") && !gender.equals("FEMALE")) {
            throw new IllegalArgumentException("Gender must be MALE or FEMALE. Got: " + gender);
        }
    }

    /**
     * Validate sponsorship type
     * 
     * @param sponsorship Sponsorship to validate (GOV, SELF)
     * @throws IllegalArgumentException if invalid
     */
    public static void validateSponsorship(String sponsorship) {
        validateNonEmpty(sponsorship, "Sponsorship");
        if (!sponsorship.equals("GOV") && !sponsorship.equals("SELF")) {
            throw new IllegalArgumentException("Sponsorship must be GOV or SELF. Got: " + sponsorship);
        }
    }

    // ========================================================================
    // Collection Validations
    // ========================================================================

    /**
     * Validate array not empty
     * 
     * @param array Array to validate
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if array is null or empty
     */
    public static <T> void validateNotEmpty(T[] array, String fieldName) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    /**
     * Validate array size
     * 
     * @param array Array to validate
     * @param expectedSize Expected size
     * @param fieldName Field name for error message
     * @throws IllegalArgumentException if size mismatch
     */
    public static <T> void validateSize(T[] array, int expectedSize, String fieldName) {
        validateNotEmpty(array, fieldName);
        if (array.length != expectedSize) {
            throw new IllegalArgumentException(
                    fieldName + " must have exactly " + expectedSize + " elements. Got: " + array.length
            );
        }
    }

    // ========================================================================
    // Complex Validations
    // ========================================================================

    /**
     * Validate year of study
     * 
     * @param yearOfStudy Year to validate (1-7)
     * @throws IllegalArgumentException if year invalid
     */
    public static void validateYearOfStudy(int yearOfStudy) {
        validateRange(yearOfStudy, 1, 7, "Year of study");
    }

    /**
     * Validate bed capacity
     * 
     * @param totalBeds Total beds
     * @param maleBeds Male beds
     * @param femaleBeds Female beds
     * @throws IllegalArgumentException if invalid
     */
    public static void validateBedCapacity(int totalBeds, int maleBeds, int femaleBeds) {
        if (totalBeds <= 0) {
            throw new IllegalArgumentException("Total beds must be positive");
        }
        if (maleBeds < 0 || femaleBeds < 0) {
            throw new IllegalArgumentException("Bed counts cannot be negative");
        }
        if (maleBeds + femaleBeds != totalBeds) {
            throw new IllegalArgumentException(
                    "Male beds (" + maleBeds + ") + Female beds (" + femaleBeds + 
                    ") must equal total beds (" + totalBeds + ")"
            );
        }
    }

    /**
     * Validate distance from home
     * 
     * @param distance Distance in km
     * @throws IllegalArgumentException if invalid
     */
    public static void validateDistance(double distance) {
        if (distance < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }
        if (distance > 10000) {
            throw new IllegalArgumentException("Distance seems unreasonable (> 10000 km)");
        }
    }

    /**
     * Validate room number format
     * 
     * @param roomNumber Room number to validate
     * @throws IllegalArgumentException if invalid
     */
    public static void validateRoomNumber(String roomNumber) {
        validateNonEmpty(roomNumber, "Room number");
        if (roomNumber.length() > 50) {
            throw new IllegalArgumentException("Room number too long (max 50 characters)");
        }
        if (!roomNumber.matches("^[A-Z0-9\\-\\/\\s]+$")) {
            throw new IllegalArgumentException("Room number contains invalid characters");
        }
    }

    // ========================================================================
    // Batch Validation
    // ========================================================================

    /**
     * Validate multiple fields at once
     * Useful for form validation
     * 
     * @param validations Array of validation tasks
     */
    public static void validateBatch(ValidationTask... validations) {
        StringBuilder errors = new StringBuilder();
        
        for (ValidationTask task : validations) {
            try {
                task.validate();
            } catch (IllegalArgumentException e) {
                if (errors.length() > 0) {
                    errors.append("\n");
                }
                errors.append("- ").append(e.getMessage());
            }
        }

        if (errors.length() > 0) {
            throw new IllegalArgumentException("Validation errors:\n" + errors.toString());
        }
    }

    /**
     * Functional interface for validation tasks
     * Allows batch validation of multiple fields
     */
    @FunctionalInterface
    public interface ValidationTask {
        void validate() throws IllegalArgumentException;
    }
}
