package edu.aau.dorm.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Password hashing utility.
 * Note: For real systems use bcrypt/argon2. For coursework, SHA-256 is acceptable.
 */
public final class PasswordHasher {
    private PasswordHasher() {}

    public static String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    public static boolean verify(String raw, String hash) {
        return hash(raw).equals(hash);
    }
}
