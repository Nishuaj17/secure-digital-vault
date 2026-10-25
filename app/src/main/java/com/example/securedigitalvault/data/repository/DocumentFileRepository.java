
package com.example.securedigitalvault.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.securedigitalvault.data.local.VaultDatabase;
import com.example.securedigitalvault.data.local.dao.DocumentFileDao;
import com.example.securedigitalvault.data.local.entity.DocumentFileEntity;
import com.example.securedigitalvault.security.CryptoManager;
import com.example.securedigitalvault.security.KeystoreManager;
import com.example.securedigitalvault.security.SecureFileManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DocumentFileRepository {

    private static final String TAG = "DocumentFileRepository";
    private static final long TEMP_FILE_LIFETIME_MILLIS = 2 * 60 * 1000;

    public interface ImportCallback {
        void onComplete(boolean success);
    }

    public interface OpenCallback {
        void onReady(File tempFile);
        void onError();
    }

    private final Context appContext;
    private final DocumentFileDao documentFileDao;
    private final SecureFileManager secureFileManager;
    private final ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public DocumentFileRepository(Context context) {
        appContext = context.getApplicationContext();

        VaultDatabase database = VaultDatabase.getInstance(appContext);
        documentFileDao = database.documentFileDao();

        CryptoManager cryptoManager = new CryptoManager(new KeystoreManager());
        secureFileManager = new SecureFileManager(cryptoManager);

        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<DocumentFileEntity>> getAllDocuments() {
        return documentFileDao.getAllDocuments();
    }

    public LiveData<List<DocumentFileEntity>> searchByFileName(String term) {
        return documentFileDao.searchByFileName(term);
    }

    public LiveData<Integer> getDocumentCountLive() {
        return documentFileDao.getDocumentCountLive();
    }

    /**
     * Encrypts the selected file, then saves its metadata.
     * Success is reported only after both operations succeed.
     */
    public void insertDocument(Uri sourceUri, ImportCallback callback) {
        executorService.execute(() -> {
            boolean success = false;
            String encryptedFileName = null;

            try {
                if (sourceUri == null) {
                    throw new IOException("No document was selected.");
                }

                String displayName = queryDisplayName(sourceUri);
                long sizeBytes = querySize(sourceUri);
                String fileType = extractFileExtension(displayName);

                encryptedFileName = secureFileManager.encryptAndStoreFile(
                        appContext,
                        sourceUri,
                        SecureFileManager.DIR_DOCUMENTS
                );

                DocumentFileEntity entity = new DocumentFileEntity(
                        displayName,
                        fileType,
                        sizeBytes,
                        encryptedFileName,
                        0,
                        System.currentTimeMillis()
                );

                long insertedId = documentFileDao.insert(entity);

                if (insertedId <= 0) {
                    throw new IOException("Document metadata was not saved.");
                }

                success = true;

            } catch (Exception e) {
                Log.e(TAG, "Failed to import document", e);

                // If metadata saving failed after encryption, remove
                // the encrypted file to avoid an orphaned file.
                if (encryptedFileName != null) {
                    try {
                        boolean deleted = secureFileManager.deleteEncryptedFile(
                                appContext,
                                encryptedFileName,
                                SecureFileManager.DIR_DOCUMENTS
                        );

                        if (!deleted) {
                            Log.w(TAG,
                                    "Could not remove an orphaned encrypted document file.");
                        }
                    } catch (Exception cleanupError) {
                        Log.e(TAG,
                                "Failed to clean up encrypted document after import failure",
                                cleanupError);
                    }
                }
            }

            boolean finalSuccess = success;

            if (callback != null) {
                mainHandler.post(() -> callback.onComplete(finalSuccess));
            }
        });
    }

    public void deleteDocument(int id) {
        executorService.execute(() -> {
            try {
                DocumentFileEntity entity =
                        documentFileDao.getDocumentById(id);

                if (entity != null) {
                    // Remove the database record first. If physical
                    // cleanup fails, the encrypted orphan is private
                    // app data, not a publicly accessible document.
                    documentFileDao.delete(entity);

                    boolean deleted = secureFileManager.deleteEncryptedFile(
                            appContext,
                            entity.encryptedFileName,
                            SecureFileManager.DIR_DOCUMENTS
                    );

                    if (!deleted) {
                        Log.w(TAG,
                                "Document record deleted, but encrypted-file cleanup failed.");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete document", e);
            }
        });
    }

    /**
     * Creates a short-lived decrypted copy for external viewing.
     */
    public void openDocumentSecurely(int id, OpenCallback callback) {
        executorService.execute(() -> {
            try {
                DocumentFileEntity entity =
                        documentFileDao.getDocumentById(id);

                if (entity == null) {
                    mainHandler.post(callback::onError);
                    return;
                }

                File tempFile = secureFileManager.decryptToTempFile(
                        appContext,
                        entity.encryptedFileName,
                        SecureFileManager.DIR_DOCUMENTS,
                        entity.fileName
                );

                mainHandler.post(() -> callback.onReady(tempFile));

                mainHandler.postDelayed(() -> {
                    if (tempFile.exists() && !tempFile.delete()) {
                        Log.w(TAG, "Could not delete temporary document file.");
                    }
                }, TEMP_FILE_LIFETIME_MILLIS);

            } catch (Exception e) {
                Log.e(TAG, "Failed to open document securely", e);
                mainHandler.post(callback::onError);
            }
        });
    }

    /**
     * Removes temporary decrypted document copies.
     */
    public void clearTempFiles() {
        try {
            secureFileManager.clearAllTempFiles(appContext);
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear temporary document files", e);
        }
    }

    private String queryDisplayName(Uri uri) throws IOException {
        ContentResolver resolver = appContext.getContentResolver();

        try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex =
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                if (nameIndex != -1) {
                    String name = cursor.getString(nameIndex);

                    if (name != null && !name.trim().isEmpty()) {
                        return new File(name).getName();
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException("Could not read the selected file name.", e);
        }

        return "document";
    }

    private long querySize(Uri uri) {
        ContentResolver resolver = appContext.getContentResolver();

        try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

                if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not read document size.", e);
        }

        return 0;
    }

    private String extractFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "file";
        }

        return fileName.substring(dotIndex + 1).toUpperCase();
    }
}
