package com.bhojanaloy.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * SHA-256 + per-user random salt password hashing.
 * Not as strong as bcrypt/argon2, but dependency-free and adequate for a
 * desktop POS app that isn't exposed to the public internet.
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String generateSalt() {
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    public static String hash(String plainPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashed = digest.digest(plainPassword.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Verifies a plaintext password against a stored salt+hash pair.
     * Returns false (instead of throwing) if the stored salt/hash is missing
     * or malformed — e.g. a placeholder value like "__SET_BY_APP__" that was
     * never replaced with a real generated salt. This prevents a corrupted
     * or not-yet-initialized user row from crashing the login screen.
     */
    public static boolean verify(String plainPassword, String salt, String expectedHash) {
        if (plainPassword == null || salt == null || expectedHash == null) return false;
        try {
            String actual = hash(plainPassword, salt);
            return actual.equals(expectedHash);
        } catch (IllegalArgumentException e) {
            // Thrown by Base64 decoding when salt isn't valid Base64
            // (e.g. still the "__SET_BY_APP__" placeholder from the sample SQL).
            System.err.println("Invalid salt/hash format for this user — was PasswordUtil ever run for this account?");
            return false;
        }
    }

    /** Handy CLI helper: prints a fresh salt+hash pair for manual SQL inserts. */
    public static void main(String[] args) {
        String pwd = args.length > 0 ? args[0] : "admin123";
        String salt = generateSalt();
        String hash = hash(pwd, salt);
        System.out.println("Password : " + pwd);
        System.out.println("Salt     : " + salt);
        System.out.println("Hash     : " + hash);
    }
}
