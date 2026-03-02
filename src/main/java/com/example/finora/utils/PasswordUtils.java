package com.example.finora.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password utility.
 * Automatically handles salting internally.
 */
public final class PasswordUtils {

    private PasswordUtils() {}

    /**
     * Hash plain password using BCrypt.
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /**
     * Verify plain password against stored hash.
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /**
     * Check if value is already hashed (BCrypt hashes start with $2a$, $2b$, or $2y$)
     */
    public static boolean isHashed(String value) {
        return value != null && value.startsWith("$2");
    }
}
