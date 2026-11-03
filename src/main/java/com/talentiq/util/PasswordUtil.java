package com.talentiq.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for hashing and verifying passwords using BCrypt.
 */
public class PasswordUtil {

    private static final int BCRYPT_LOG_ROUNDS = 12;

    private PasswordUtil() {
        // Utility class
    }

    /**
     * Hashes a plain-text password with BCrypt.
     *
     * @param plainTextPassword plain-text string
     * @return salted and hashed password
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    /**
     * Checks if a plain-text password matches a hashed password.
     *
     * @param plainTextPassword candidate plain-text password
     * @param hashedPassword    stored BCrypt hash
     * @return true if matches, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        String[] passwords = {"Admin@123", "Recruiter@123", "Candidate@123", "Password@123"};
        for (String p : passwords) {
            System.out.println(p + " => " + hashPassword(p));
        }
    }
}
