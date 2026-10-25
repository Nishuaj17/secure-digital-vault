
package com.example.securedigitalvault.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.securedigitalvault.data.local.VaultDatabase;
import com.example.securedigitalvault.data.local.dao.RecoveryCodeDao;
import com.example.securedigitalvault.data.local.entity.RecoveryCodeEntity;
import com.example.securedigitalvault.security.CryptoManager;
import com.example.securedigitalvault.security.KeystoreManager;
import com.example.securedigitalvault.ui.recovery.DecryptedRecoveryCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecoveryCodeRepository {

    private static final String TAG = "RecoveryCodeRepository";

    public interface RecoveryCodeCallback {
        void onResult(DecryptedRecoveryCode entry);
    }

    public interface SaveCallback {
        void onComplete(boolean success);
    }

    private final RecoveryCodeDao recoveryCodeDao;
    private final CryptoManager cryptoManager;
    private final ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public RecoveryCodeRepository(Context context) {
        VaultDatabase database = VaultDatabase.getInstance(context);
        recoveryCodeDao = database.recoveryCodeDao();
        cryptoManager = new CryptoManager(new KeystoreManager());
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<DecryptedRecoveryCode>> getAllEntries() {
        return Transformations.map(
                recoveryCodeDao.getAllEntries(),
                this::decryptList
        );
    }

    public LiveData<Integer> getEntryCountLive() {
        return recoveryCodeDao.getEntryCountLive();
    }

    // Backward-compatible method for existing callers.
    public void insertEntry(
            String serviceName,
            String codes,
            String notes,
            int categoryId,
            boolean isFavorite
    ) {
        insertEntry(serviceName, codes, notes, categoryId, isFavorite, null);
    }

    // Callback reports whether encryption and database insertion succeeded.
    public void insertEntry(
            String serviceName,
            String codes,
            String notes,
            int categoryId,
            boolean isFavorite,
            SaveCallback callback
    ) {
        executorService.execute(() -> {
            boolean success = false;

            try {
                long now = System.currentTimeMillis();

                byte[] encryptedCodes = cryptoManager.encryptString(
                        codes != null ? codes : ""
                );
                byte[] encryptedNotes = cryptoManager.encryptString(
                        notes != null ? notes : ""
                );

                RecoveryCodeEntity entity = new RecoveryCodeEntity(
                        serviceName,
                        categoryId,
                        isFavorite,
                        now,
                        now,
                        encryptedCodes,
                        encryptedNotes
                );

                success = recoveryCodeDao.insert(entity) > 0;

            } catch (Exception e) {
                // Never log recovery-code contents.
                Log.e(TAG, "Failed to save recovery codes", e);
            }

            notifySaveCallback(callback, success);
        });
    }

    // Backward-compatible method for existing callers.
    public void updateEntry(DecryptedRecoveryCode decrypted) {
        updateEntry(decrypted, null);
    }

    // Callback reports whether encryption and database update succeeded.
    public void updateEntry(
            DecryptedRecoveryCode decrypted,
            SaveCallback callback
    ) {
        executorService.execute(() -> {
            boolean success = false;

            try {
                byte[] encryptedCodes = cryptoManager.encryptString(
                        decrypted.codes != null ? decrypted.codes : ""
                );
                byte[] encryptedNotes = cryptoManager.encryptString(
                        decrypted.notes != null ? decrypted.notes : ""
                );

                RecoveryCodeEntity entity = new RecoveryCodeEntity(
                        decrypted.serviceName,
                        decrypted.categoryId,
                        decrypted.isFavorite,
                        decrypted.createdAt,
                        System.currentTimeMillis(),
                        encryptedCodes,
                        encryptedNotes
                );

                entity.id = decrypted.id;
                recoveryCodeDao.update(entity);
                success = true;

            } catch (Exception e) {
                Log.e(TAG, "Failed to update recovery codes", e);
            }

            notifySaveCallback(callback, success);
        });
    }

    public void deleteEntry(int id) {
        executorService.execute(() -> {
            try {
                RecoveryCodeEntity entity = recoveryCodeDao.getEntryById(id);

                if (entity != null) {
                    recoveryCodeDao.delete(entity);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete recovery codes", e);
            }
        });
    }

    public void getEntryByIdAsync(
            int id,
            RecoveryCodeCallback callback
    ) {
        executorService.execute(() -> {
            try {
                RecoveryCodeEntity entity =
                        recoveryCodeDao.getEntryById(id);

                if (entity == null) {
                    mainHandler.post(() -> callback.onResult(null));
                    return;
                }

                String codes = cryptoManager.decryptToString(
                        entity.encryptedCodes
                );
                String notes = cryptoManager.decryptToString(
                        entity.encryptedNotes
                );

                DecryptedRecoveryCode decrypted =
                        new DecryptedRecoveryCode(
                                entity.id,
                                entity.serviceName,
                                codes,
                                notes,
                                entity.categoryId,
                                entity.isFavorite,
                                entity.createdAt,
                                entity.updatedAt
                        );

                mainHandler.post(() -> callback.onResult(decrypted));

            } catch (Exception e) {
                Log.e(TAG, "Failed to load recovery-code entry", e);
                mainHandler.post(() -> callback.onResult(null));
            }
        });
    }

    private List<DecryptedRecoveryCode> decryptList(
            List<RecoveryCodeEntity> entities
    ) {
        List<DecryptedRecoveryCode> result = new ArrayList<>();

        for (RecoveryCodeEntity entity : entities) {
            try {
                String codes = cryptoManager.decryptToString(
                        entity.encryptedCodes
                );
                String notes = cryptoManager.decryptToString(
                        entity.encryptedNotes
                );

                result.add(new DecryptedRecoveryCode(
                        entity.id,
                        entity.serviceName,
                        codes,
                        notes,
                        entity.categoryId,
                        entity.isFavorite,
                        entity.createdAt,
                        entity.updatedAt
                ));

            } catch (Exception e) {
                // Don't log or expose the recovery-code contents.
                Log.e(TAG,
                        "Could not decrypt recovery-code entry ID "
                                + entity.id,
                        e);
            }
        }

        return result;
    }

    public void toggleFavorite(int id, boolean isFavorite) {
        executorService.execute(() -> {
            try {
                recoveryCodeDao.updateFavoriteStatus(id, isFavorite);
            } catch (Exception e) {
                Log.e(TAG, "Failed to update recovery-code favorite", e);
            }
        });
    }

    private void notifySaveCallback(
            SaveCallback callback,
            boolean success
    ) {
        if (callback != null) {
            mainHandler.post(() -> callback.onComplete(success));
        }
    }
}
