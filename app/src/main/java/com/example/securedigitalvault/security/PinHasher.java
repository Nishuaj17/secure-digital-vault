package com.example.securedigitalvault.security;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Locale;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PinHasher
 *
 * Responsible for turning a user's PIN into a secure, one-way hash
 * using PBKDF2WithHmacSHA256, and later verifying a typed PIN against
 * that stored hash.
 *
 * IMPORTANT SECURITY NOTES:
 * - The raw PIN is NEVER stored anywhere (not in SharedPreferences,
 *   not in the database, not in logs).
 * - Each PIN gets its own random "salt" so identical PINs from
 *   different setups never produce identical hashes.
 * - We use a high iteration count to slow down brute-force attacks.
 * - This class only handles PIN verification. It does NOT generate
 *   the actual AES encryption key used for your vault data — that is
 *   handled separately by KeystoreManager in a later step. Keeping
 *   these two concerns separate means changing your PIN later does not
 *   require re-encrypting your entire vault.
 */
public class PinHasher {

    // PBKDF2 configuration constants.
    // 10,000 iterations is a reasonable baseline for a PIN-length secret
    // on a mobile device without causing a noticeable delay to the user.
    private static final int ITERATION_COUNT = 10000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH_BYTES = 16;

    /**
     * Generates a new random salt. Call this ONCE when the user first
     * creates their PIN, then store the salt (it is safe to store
     * alongside the hash — a salt is not secret, it just needs to be
     * unique and random).
     */
    public static byte[] generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a PIN using PBKDF2WithHmacSHA256 with the given salt.
     *
     * @param pin  the plaintext PIN typed by the user (char array is
     *             used instead of String so we can wipe it from memory
     *             immediately after use — Strings are immutable in Java
     *             and can linger in memory until garbage collected).
     * @param salt the random salt associated with this user's PIN.
     * @return a Base64-encoded string representation of the hash,
     *         safe to store in SharedPreferences or Room.
     */
    public static String hashPin(char[] pin, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(pin, salt, ITERATION_COUNT, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hashBytes = factory.generateSecret(spec).getEncoded();
            return Base64.encodeToString(hashBytes, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // This should practically never happen on a real Android device
            // since PBKDF2WithHmacSHA256 is a standard supported algorithm.
            // We deliberately do NOT log the exception message here in case
            // it somehow contains sensitive data — just a safe generic message.
            throw new RuntimeException("Unable to hash PIN due to a security provider error.");
        } finally {
            // Wipe the PIN from memory as soon as we're done with it.
            // This is a defense-in-depth measure — it reduces the window
            // during which the PIN exists in plaintext in memory.
            java.util.Arrays.fill(pin, '\u0000');
        }
    }

    /**
     * Verifies a typed PIN against a previously stored hash + salt.
     *
     * @param typedPin    the PIN the user just typed on the lock screen.
     * @param storedHash  the Base64-encoded hash saved during setup.
     * @param salt        the salt saved during setup.
     * @return true if the typed PIN matches, false otherwise.
     */
    public static boolean verifyPin(char[] typedPin, String storedHash, byte[] salt) {
        String newHash = hashPin(typedPin, salt);
        return constantTimeEquals(newHash, storedHash);
    }

    /**
     * Compares two strings in constant time to avoid "timing attacks" —
     * where an attacker measures how long a comparison takes to guess
     * the correct value character-by-character. A normal String.equals()
     * can exit early on the first mismatched character, leaking timing
     * information. This method always checks every character.
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * Utility to Base64-encode a salt for storage.
     */
    public static String encodeSalt(byte[] salt) {
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    /**
     * Utility to decode a stored Base64 salt back into bytes.
     */
    public static byte[] decodeSalt(String encodedSalt) {
        return Base64.decode(encodedSalt, Base64.NO_WRAP);
    }
}

