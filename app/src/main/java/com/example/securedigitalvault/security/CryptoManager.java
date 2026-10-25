
package com.example.securedigitalvault.security;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Provides AES-256-GCM encryption and decryption for sensitive vault data
 * using a Keystore-backed key from KeystoreManager.
 *
 * Encrypted output format:
 * [12-byte IV][ciphertext + 16-byte GCM authentication tag]
 */
public class CryptoManager {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final KeystoreManager keystoreManager;

    public CryptoManager(KeystoreManager keystoreManager) {
        this.keystoreManager = keystoreManager;
    }

    /**
     * Encrypts raw bytes and returns [IV][ciphertext + tag].
     * Android Keystore generates the IV during encryption.
     */
    public byte[] encrypt(byte[] plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException(
                    "Plaintext cannot be null."
            );
        }

        try {
            SecretKey secretKey =
                    keystoreManager.getOrCreateSecretKey();

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            // Let Android Keystore generate a fresh IV.
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();

            if (iv == null || iv.length != IV_LENGTH_BYTES) {
                throw new IllegalStateException(
                        "Android Keystore returned an invalid AES-GCM IV."
                );
            }

            byte[] ciphertext = cipher.doFinal(plaintext);

            // Combine IV and ciphertext for database/file storage.
            byte[] combined = new byte[iv.length + ciphertext.length];

            System.arraycopy(
                    iv, 0,
                    combined, 0,
                    iv.length
            );

            System.arraycopy(
                    ciphertext, 0,
                    combined, iv.length,
                    ciphertext.length
            );

            return combined;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Encryption failed.",
                    e
            );
        }
    }

    /**
     * Encrypts a String using UTF-8 encoding.
     */
    public byte[] encryptString(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException(
                    "Plaintext cannot be null."
            );
        }

        return encrypt(
                plaintext.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Decrypts an [IV][ciphertext + tag] byte array.
     * Throws an exception if the data is invalid or authentication fails.
     */
    public byte[] decrypt(byte[] combined) {
        try {
            // A valid AES-GCM payload requires a 12-byte IV
            // and a 16-byte authentication tag at minimum.
            if (combined == null
                    || combined.length < IV_LENGTH_BYTES + 16) {
                throw new IllegalArgumentException(
                        "Encrypted data is too short to be valid."
                );
            }

            byte[] iv = Arrays.copyOfRange(
                    combined,
                    0,
                    IV_LENGTH_BYTES
            );

            byte[] ciphertext = Arrays.copyOfRange(
                    combined,
                    IV_LENGTH_BYTES,
                    combined.length
            );

            SecretKey secretKey =
                    keystoreManager.getOrCreateSecretKey();

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH_BITS,
                            iv
                    );

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    gcmSpec
            );

            return cipher.doFinal(ciphertext);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Decryption failed. Data may be corrupted or tampered with.",
                    e
            );
        }
    }

    /**
     * Decrypts encrypted data and converts it back to a UTF-8 String.
     */
    public String decryptToString(byte[] combined) {
        return new String(
                decrypt(combined),
                StandardCharsets.UTF_8
        );
    }
}
