
package com.example.securedigitalvault.data.repository;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.securedigitalvault.data.local.VaultDatabase;
import com.example.securedigitalvault.data.local.dao.PhotoDao;
import com.example.securedigitalvault.data.local.entity.PhotoEntity;
import com.example.securedigitalvault.security.CryptoManager;
import com.example.securedigitalvault.security.KeystoreManager;
import com.example.securedigitalvault.security.SecureFileManager;
import com.example.securedigitalvault.ui.photos.DecryptedPhoto;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhotoRepository {

    private static final String TAG = "PhotoRepository";

    public interface PhotoCallback {
        void onResult(DecryptedPhoto photo);
    }

    public interface SimpleCallback {
        void onComplete(boolean success);
    }

    private final Context appContext;
    private final PhotoDao photoDao;
    private final SecureFileManager secureFileManager;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public PhotoRepository(Context context) {
        this.appContext = context.getApplicationContext();

        VaultDatabase database = VaultDatabase.getInstance(appContext);
        this.photoDao = database.photoDao();

        CryptoManager cryptoManager = new CryptoManager(new KeystoreManager());
        this.secureFileManager = new SecureFileManager(cryptoManager);

        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Returns photo metadata. Image contents remain encrypted
     * in private app storage until a photo is opened.
     */
    public LiveData<List<PhotoEntity>> getAllPhotoMetadata() {
        return photoDao.getAllPhotos();
    }

    public LiveData<Integer> getPhotoCountLive() {
        return photoDao.getPhotoCountLive();
    }

    /**
     * Reads the original display name from the selected photo URI.
     * Returns "Photo" if the name cannot be retrieved.
     */
    private String getPhotoDisplayName(Uri uri) {
        if (uri == null) {
            return "Photo";
        }

        try (Cursor cursor = appContext.getContentResolver().query(
                uri,
                new String[]{OpenableColumns.DISPLAY_NAME},
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex =
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                if (nameIndex >= 0) {
                    String name = cursor.getString(nameIndex);

                    if (name != null && !name.trim().isEmpty()) {
                        // Store only the filename, not any path.
                        return new java.io.File(name).getName();
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not read the selected photo's name.", e);
        }

        return "Photo";
    }

    /**
     * Encrypts and stores a selected photo.
     * If saving its database record fails, attempts to remove
     * the encrypted file created during this operation.
     */
    public void insertPhoto(Uri sourcePhotoUri, SimpleCallback callback) {
        executorService.execute(() -> {
            boolean success = false;
            String encryptedFileName = null;

            try {
                if (sourcePhotoUri == null) {
                    throw new IOException("No photo was selected.");
                }

                String displayName = getPhotoDisplayName(sourcePhotoUri);

                encryptedFileName = secureFileManager.encryptAndStoreFile(
                        appContext,
                        sourcePhotoUri,
                        SecureFileManager.DIR_PHOTOS
                );

                if (encryptedFileName == null
                        || encryptedFileName.trim().isEmpty()) {
                    throw new IOException(
                            "The encrypted photo file was not created."
                    );
                }

                PhotoEntity entity = new PhotoEntity(
                        displayName,
                        encryptedFileName,
                        0,
                        System.currentTimeMillis()
                );

                long insertedId = photoDao.insert(entity);

                if (insertedId <= 0) {
                    throw new IOException(
                            "The photo record could not be saved."
                    );
                }

                success = true;

            } catch (Exception e) {
                Log.e(TAG, "Failed to save photo securely.", e);

                if (!success && encryptedFileName != null) {
                    try {
                        boolean deleted =
                                secureFileManager.deleteEncryptedFile(
                                        appContext,
                                        encryptedFileName,
                                        SecureFileManager.DIR_PHOTOS
                                );

                        if (!deleted) {
                            Log.w(
                                    TAG,
                                    "Could not clean up the encrypted photo file."
                            );
                        }
                    } catch (Exception cleanupError) {
                        Log.e(
                                TAG,
                                "Failed to clean up the encrypted photo file.",
                                cleanupError
                        );
                    }
                }
            }

            final boolean result = success;

            if (callback != null) {
                mainHandler.post(() -> callback.onComplete(result));
            }
        });
    }

    /**
     * Deletes the photo's database record and then attempts
     * to remove its encrypted file.
     */
    public void deletePhoto(int id) {
        executorService.execute(() -> {
            try {
                PhotoEntity entity = photoDao.getPhotoById(id);

                if (entity == null) {
                    Log.w(TAG, "Photo not found for deletion: " + id);
                    return;
                }

                photoDao.delete(entity);

                boolean fileDeleted =
                        secureFileManager.deleteEncryptedFile(
                                appContext,
                                entity.encryptedFileName,
                                SecureFileManager.DIR_PHOTOS
                        );

                if (!fileDeleted) {
                    Log.w(
                            TAG,
                            "Photo record deleted, but encrypted file cleanup failed."
                    );
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to delete photo: " + id, e);
            }
        });
    }

    /**
     * Decrypts a photo into memory for display.
     */
    public void decryptPhotoByIdAsync(int id, PhotoCallback callback) {
        executorService.execute(() -> {
            DecryptedPhoto decryptedPhoto = null;

            try {
                PhotoEntity entity = photoDao.getPhotoById(id);

                if (entity == null) {
                    Log.w(TAG, "Photo not found: " + id);
                } else {
                    byte[] imageBytes =
                            secureFileManager.decryptFileBytes(
                                    appContext,
                                    entity.encryptedFileName,
                                    SecureFileManager.DIR_PHOTOS
                            );

                    if (imageBytes != null && imageBytes.length > 0) {
                        decryptedPhoto = new DecryptedPhoto(
                                entity.id,
                                entity.displayName,
                                entity.dateAdded,
                                imageBytes
                        );
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to decrypt photo: " + id, e);
            }

            final DecryptedPhoto result = decryptedPhoto;

            if (callback != null) {
                mainHandler.post(() -> callback.onResult(result));
            }
        });
    }
}
