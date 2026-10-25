package com.example.securedigitalvault.security;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.KeyStore;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * KeystoreManager
 *
 * Responsible for generating and retrieving a single AES-256 secret key
 * that lives inside the Android Keystore. This key is used by
 * CryptoManager to encrypt/decrypt all sensitive vault data (passwords,
 * notes, card details, documents, etc).
 *
 * KEY SECURITY PROPERTIES:
 * - The key is generated INSIDE the Keystore itself (setKeyGenParameterSpec),
 *   meaning the raw key bytes never exist in your app's Java heap.
 * - On devices with a Trusted Execution Environment (TEE) or StrongBox
 *   (dedicated secure hardware chip), the key material never leaves
 *   that hardware at all — not even the OS kernel can extract it.
 * - We use AES-256 in GCM mode, which provides both confidentiality
 *   (data is unreadable without the key) AND integrity (any tampering
 *   with the ciphertext is detected and decryption will fail loudly
 *   rather than silently returning corrupted data).
 *
 * WHAT THIS CLASS DOES NOT DO:
 * - It does not require biometric authentication to use the key
 *   (setUserAuthenticationRequired is intentionally left false here).
 *   That is a deliberate choice for this phase: vault data needs to be
 *   decryptable whenever the user is inside the app (after PIN or
 *   biometric login at the Activity level), not only in the exact
 *   instant a fingerprint sensor is touched. The PIN/biometric check
 *   at LockScreenActivity is what gates entry to the app; this key
 *   protects the data at rest on disk.
 */
public class KeystoreManager {

    private static final String ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS = "secure_vault_master_key";
    private static final int KEY_SIZE_BITS = 256;

    /**
     * Returns the app's master AES key, generating it on first call if
     * it doesn't already exist. Safe to call repeatedly — after the
     * first call, subsequent calls simply retrieve the existing key
     * from the Keystore rather than creating a new one (which would
     * make previously encrypted data undecryptable).
     */
    public SecretKey getOrCreateSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER);
            keyStore.load(null);

            if (keyStore.containsAlias(KEY_ALIAS)) {
                KeyStore.SecretKeyEntry entry =
                        (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
                return entry.getSecretKey();
            }

            return generateNewKey();

        } catch (Exception e) {
            // We intentionally catch broadly here because Keystore
            // operations can throw several different checked exceptions
            // depending on the Android version and device. We never log
            // the exception details in case they somehow reference key
            // material, and we surface a clear, generic failure instead.
            throw new RuntimeException("Unable to access secure key storage.", e);
        }
    }

    /**
     * Generates a brand new AES-256-GCM key inside the Android Keystore.
     * Only called once, the very first time the app needs to encrypt
     * anything (typically right after PIN setup completes).
     */
    private SecretKey generateNewKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE_PROVIDER);

        KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                // setUserAuthenticationRequired(false): see class-level
                // comment above for why. This key protects data at rest,
                // separately from the PIN/biometric app-entry gate.
                .setUserAuthenticationRequired(false)
                .build();

        keyGenerator.init(keySpec);
        return keyGenerator.generateKey();
    }

    /**
     * Deletes the master key from the Keystore. Used only in the
     * "reset vault" recovery flow (Settings > forgot PIN scenario),
     * where the user explicitly chooses to wipe all vault data because
     * they can no longer prove their identity. This is irreversible —
     * any data encrypted with this key becomes permanently undecryptable
     * the moment this is called.
     */
    public void deleteSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER);
            keyStore.load(null);
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to delete secure key.", e);
        }
    }
}


