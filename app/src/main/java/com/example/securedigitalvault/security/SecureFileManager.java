package com.example.securedigitalvault.security;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * SecureFileManager
 *
 * Handles encrypting files (photos, PDFs/documents) before writing
 * them to the app's private storage, and decrypting them back on
 * demand. Generalized to support multiple sub-directories so photos
 * and documents don't mix, while sharing the same encryption logic.
 *
 * STORAGE LOCATIONS:
 *   context.getFilesDir()/vault_photos/     — encrypted photos, permanent
 *   context.getFilesDir()/vault_documents/  — encrypted PDFs/docs, permanent
 *   context.getCacheDir()/temp_docs/        — SHORT-LIVED decrypted PDF
 *                                              copies, only for handing
 *                                              off to an external PDF
 *                                              viewer app, cleaned up
 *                                              automatically
 *
 * All of these are private to this app — never public/shared storage,
 * and never indexed by MediaStore or the system gallery.
 */
public class SecureFileManager {

    public static final String DIR_PHOTOS = "vault_photos";
    public static final String DIR_DOCUMENTS = "vault_documents";
    private static final String TEMP_DOCS_DIR = "temp_docs";

    private final CryptoManager cryptoManager;

    public SecureFileManager(CryptoManager cryptoManager) {
        this.cryptoManager = cryptoManager;
    }

    private File getDirectory(Context context, String subDirName) {
        File dir = new File(context.getFilesDir(), subDirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Reads a file the user picked, encrypts its bytes, and writes the
     * ciphertext into the given private sub-directory. Returns the
     * generated filename to store in the database.
     */
    public String encryptAndStoreFile(Context context, Uri sourceUri, String subDirName) throws IOException {
        byte[] plaintextBytes = readAllBytesFromUri(context, sourceUri);
        byte[] encryptedBytes = cryptoManager.encrypt(plaintextBytes);

        String fileName = UUID.randomUUID().toString() + ".enc";
        File destinationFile = new File(getDirectory(context, subDirName), fileName);

        try (FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
            outputStream.write(encryptedBytes);
        }

        return fileName;
    }

    /**
     * Reads and decrypts a stored file, returning plaintext bytes
     * directly in memory. Used for photos, which are decoded straight
     * into a Bitmap without ever touching disk in decrypted form.
     */
    public byte[] decryptFileBytes(Context context, String fileName, String subDirName) throws IOException {
        File sourceFile = new File(getDirectory(context, subDirName), fileName);
        byte[] encryptedBytes = readAllBytesFromFile(sourceFile);
        return cryptoManager.decrypt(encryptedBytes);
    }

    /**
     * Decrypts a stored document into a SHORT-LIVED temporary file
     * inside the cache directory, for the sole purpose of handing it
     * to an external viewer app via FileProvider. The caller is
     * responsible for deleting this file after use (see
     * DocumentFileRepository, which schedules automatic cleanup).
     */
    public File decryptToTempFile(Context context, String fileName, String subDirName,
                                  String originalDisplayName) throws IOException {
        byte[] plaintextBytes = decryptFileBytes(context, fileName, subDirName);

        File tempDir = new File(context.getCacheDir(), TEMP_DOCS_DIR);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // Use the original display name so the external viewer app
        // shows a sensible file name/extension, but prefix with a
        // random component to avoid collisions between multiple opens.
        File tempFile = new File(tempDir, UUID.randomUUID() + "_" + originalDisplayName);
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            outputStream.write(plaintextBytes);
        }

        return tempFile;
    }

    /**
     * Deletes every file currently in the temporary decrypted-docs
     * cache directory. Called on app startup/lock and after a delay
     * following each document open, so decrypted copies never
     * accumulate or persist longer than necessary.
     */
    public void clearAllTempFiles(Context context) {
        File tempDir = new File(context.getCacheDir(), TEMP_DOCS_DIR);
        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public boolean deleteEncryptedFile(Context context, String fileName, String subDirName) {
        File file = new File(getDirectory(context, subDirName), fileName);
        return file.exists() && file.delete();
    }

    private byte[] readAllBytesFromUri(Context context, Uri uri) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("Could not open input stream for the selected file.");
            }
            return readAllBytesFromStream(inputStream);
        }
    }

    private byte[] readAllBytesFromFile(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return readAllBytesFromStream(inputStream);
        }
    }

    private byte[] readAllBytesFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(chunk)) != -1) {
            buffer.write(chunk, 0, bytesRead);
        }
        return buffer.toByteArray();
    }
}

