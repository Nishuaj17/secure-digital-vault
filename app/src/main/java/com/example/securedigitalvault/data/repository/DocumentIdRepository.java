
package com.example.securedigitalvault.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.securedigitalvault.data.local.VaultDatabase;
import com.example.securedigitalvault.data.local.dao.DocumentIdDao;
import com.example.securedigitalvault.data.local.entity.DocumentIdEntity;
import com.example.securedigitalvault.security.CryptoManager;
import com.example.securedigitalvault.security.KeystoreManager;
import com.example.securedigitalvault.ui.documents.DecryptedDocumentId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DocumentIdRepository {

    private static final String TAG = "DocumentIdRepository";

    public interface DocumentCallback {
        void onResult(DecryptedDocumentId document);
    }

    public interface SaveCallback {
        void onComplete(boolean success);
    }

    private final DocumentIdDao documentDao;
    private final CryptoManager cryptoManager;
    private final ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public DocumentIdRepository(Context context) {
        VaultDatabase database = VaultDatabase.getInstance(context);
        documentDao = database.documentIdDao();
        cryptoManager = new CryptoManager(new KeystoreManager());
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<DecryptedDocumentId>> getAllDocuments() {
        return Transformations.map(
                documentDao.getAllDocuments(),
                this::decryptList
        );
    }

    public LiveData<Integer> getDocumentCountLive() {
        return documentDao.getDocumentCountLive();
    }

    // Existing signature retained for callers that do not need a callback.
    public void insertDocument(
            String name,
            String type,
            String number,
            String issueDate,
            String expiryDate,
            String notes,
            int categoryId,
            boolean isFavorite
    ) {
        insertDocument(
                name, type, number, issueDate, expiryDate,
                notes, categoryId, isFavorite, null
        );
    }

    // Reports success only after the database insert finishes.
    public void insertDocument(
            String name,
            String type,
            String number,
            String issueDate,
            String expiryDate,
            String notes,
            int categoryId,
            boolean isFavorite,
            SaveCallback callback
    ) {
        executorService.execute(() -> {
            boolean success = false;

            try {
                long now = System.currentTimeMillis();

                byte[] encryptedNumber = cryptoManager.encryptString(number);
                byte[] encryptedNotes = cryptoManager.encryptString(
                        notes != null ? notes : ""
                );

                DocumentIdEntity entity = new DocumentIdEntity(
                        name,
                        type,
                        issueDate,
                        expiryDate,
                        categoryId,
                        isFavorite,
                        now,
                        now,
                        encryptedNumber,
                        encryptedNotes
                );

                documentDao.insert(entity);
                success = true;

            } catch (Exception e) {
                Log.e(TAG, "Failed to save ID/document entry", e);
            }

            notifySaveCallback(callback, success);
        });
    }

    // Existing signature retained for callers that do not need a callback.
    public void updateDocument(DecryptedDocumentId decrypted) {
        updateDocument(decrypted, null);
    }

    // Reports success only after the database update finishes.
    public void updateDocument(
            DecryptedDocumentId decrypted,
            SaveCallback callback
    ) {
        executorService.execute(() -> {
            boolean success = false;

            try {
                byte[] encryptedNumber =
                        cryptoManager.encryptString(decrypted.documentNumber);

                byte[] encryptedNotes = cryptoManager.encryptString(
                        decrypted.notes != null ? decrypted.notes : ""
                );

                DocumentIdEntity entity = new DocumentIdEntity(
                        decrypted.documentName,
                        decrypted.documentType,
                        decrypted.issueDate,
                        decrypted.expiryDate,
                        decrypted.categoryId,
                        decrypted.isFavorite,
                        decrypted.createdAt,
                        System.currentTimeMillis(),
                        encryptedNumber,
                        encryptedNotes
                );

                entity.id = decrypted.id;
                documentDao.update(entity);
                success = true;

            } catch (Exception e) {
                Log.e(TAG, "Failed to update ID/document entry", e);
            }

            notifySaveCallback(callback, success);
        });
    }

    public void deleteDocument(int id) {
        executorService.execute(() -> {
            try {
                DocumentIdEntity entity = documentDao.getDocumentById(id);
                if (entity != null) {
                    documentDao.delete(entity);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete ID/document entry", e);
            }
        });
    }

    public void getDocumentByIdAsync(int id, DocumentCallback callback) {
        executorService.execute(() -> {
            try {
                DocumentIdEntity entity = documentDao.getDocumentById(id);

                if (entity == null) {
                    mainHandler.post(() -> callback.onResult(null));
                    return;
                }

                String number = cryptoManager.decryptToString(
                        entity.encryptedDocumentNumber
                );
                String notes = cryptoManager.decryptToString(
                        entity.encryptedNotes
                );

                DecryptedDocumentId decrypted = new DecryptedDocumentId(
                        entity.id,
                        entity.documentName,
                        entity.documentType,
                        number,
                        entity.issueDate,
                        entity.expiryDate,
                        notes,
                        entity.categoryId,
                        entity.isFavorite,
                        entity.createdAt,
                        entity.updatedAt
                );

                mainHandler.post(() -> callback.onResult(decrypted));

            } catch (Exception e) {
                Log.e(TAG, "Failed to load ID/document entry", e);
                mainHandler.post(() -> callback.onResult(null));
            }
        });
    }

    private List<DecryptedDocumentId> decryptList(
            List<DocumentIdEntity> entities
    ) {
        List<DecryptedDocumentId> result = new ArrayList<>();

        for (DocumentIdEntity entity : entities) {
            try {
                String number = cryptoManager.decryptToString(
                        entity.encryptedDocumentNumber
                );
                String notes = cryptoManager.decryptToString(
                        entity.encryptedNotes
                );

                result.add(new DecryptedDocumentId(
                        entity.id,
                        entity.documentName,
                        entity.documentType,
                        number,
                        entity.issueDate,
                        entity.expiryDate,
                        notes,
                        entity.categoryId,
                        entity.isFavorite,
                        entity.createdAt,
                        entity.updatedAt
                ));

            } catch (Exception e) {
                Log.e(TAG, "Failed to decrypt ID/document entry", e);
            }
        }

        return result;
    }

    public void toggleFavorite(int id, boolean isFavorite) {
        executorService.execute(() -> {
            try {
                documentDao.updateFavoriteStatus(id, isFavorite);
            } catch (Exception e) {
                Log.e(TAG, "Failed to update favorite status", e);
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
