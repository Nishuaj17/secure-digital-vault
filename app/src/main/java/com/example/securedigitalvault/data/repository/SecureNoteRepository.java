package com.example.securedigitalvault.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.securedigitalvault.data.local.VaultDatabase;
import com.example.securedigitalvault.data.local.dao.SecureNoteDao;
import com.example.securedigitalvault.data.local.entity.SecureNoteEntity;
import com.example.securedigitalvault.security.CryptoManager;
import com.example.securedigitalvault.security.KeystoreManager;
import com.example.securedigitalvault.ui.notes.DecryptedNote;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SecureNoteRepository {

    public interface NoteCallback {
        void onResult(DecryptedNote note);
    }

    public interface SaveCallback {
        void onComplete(boolean success, String errorMessage);
    }

    private final SecureNoteDao noteDao;
    private final CryptoManager cryptoManager;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public SecureNoteRepository(Context context) {
        VaultDatabase database = VaultDatabase.getInstance(context);
        this.noteDao = database.secureNoteDao();
        this.cryptoManager = new CryptoManager(new KeystoreManager());
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public LiveData<List<DecryptedNote>> getAllNotes() {
        return Transformations.map(
                noteDao.getAllNotes(),
                this::decryptList
        );
    }

    public LiveData<Integer> getNoteCountLive() {
        return noteDao.getNoteCountLive();
    }

    // Backward-compatible method for existing callers.
    public void insertNote(
            String title,
            String content,
            int categoryId,
            boolean isFavorite
    ) {
        insertNote(title, content, categoryId, isFavorite, null);
    }

    public void insertNote(
            String title,
            String content,
            int categoryId,
            boolean isFavorite,
            SaveCallback callback
    ) {
        executorService.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                byte[] encryptedContent = cryptoManager.encryptString(content);

                if (encryptedContent == null) {
                    throw new IllegalStateException(
                            "Note content could not be encrypted."
                    );
                }

                SecureNoteEntity entity = new SecureNoteEntity(
                        title,
                        categoryId,
                        isFavorite,
                        now,
                        now,
                        encryptedContent
                );

                noteDao.insert(entity);
                notifySaveResult(callback, true, null);

            } catch (Exception e) {
                android.util.Log.e(
                        "SecureNoteRepository",
                        "Failed to insert note",
                        e
                );

                notifySaveResult(
                        callback,
                        false,
                        "Could not save the note. Please try again."
                );
            }
        });
    }

    // Backward-compatible method for existing callers.
    public void updateNote(DecryptedNote decrypted) {
        updateNote(decrypted, null);
    }

    public void updateNote(
            DecryptedNote decrypted,
            SaveCallback callback
    ) {
        executorService.execute(() -> {
            try {
                if (decrypted == null) {
                    throw new IllegalArgumentException("Note is missing.");
                }

                byte[] encryptedContent =
                        cryptoManager.encryptString(decrypted.content);

                if (encryptedContent == null) {
                    throw new IllegalStateException(
                            "Note content could not be encrypted."
                    );
                }

                SecureNoteEntity entity = new SecureNoteEntity(
                        decrypted.title,
                        decrypted.categoryId,
                        decrypted.isFavorite,
                        decrypted.createdAt,
                        System.currentTimeMillis(),
                        encryptedContent
                );

                entity.id = decrypted.id;
                noteDao.update(entity);

                notifySaveResult(callback, true, null);

            } catch (Exception e) {
                android.util.Log.e(
                        "SecureNoteRepository",
                        "Failed to update note",
                        e
                );

                notifySaveResult(
                        callback,
                        false,
                        "Could not update the note. Please try again."
                );
            }
        });
    }

    private void notifySaveResult(
            SaveCallback callback,
            boolean success,
            String errorMessage
    ) {
        if (callback != null) {
            mainHandler.post(
                    () -> callback.onComplete(success, errorMessage)
            );
        }
    }

    public void deleteNote(int id) {
        executorService.execute(() -> {
            SecureNoteEntity entity = noteDao.getNoteById(id);

            if (entity != null) {
                noteDao.delete(entity);
            }
        });
    }

    public void getNoteByIdAsync(int id, NoteCallback callback) {
        executorService.execute(() -> {
            SecureNoteEntity entity = noteDao.getNoteById(id);

            if (entity == null) {
                mainHandler.post(() -> callback.onResult(null));
                return;
            }

            try {
                String content =
                        cryptoManager.decryptToString(entity.encryptedContent);

                DecryptedNote decrypted = new DecryptedNote(
                        entity.id,
                        entity.title,
                        content,
                        entity.categoryId,
                        entity.isFavorite,
                        entity.createdAt,
                        entity.updatedAt
                );

                mainHandler.post(() -> callback.onResult(decrypted));

            } catch (Exception e) {
                android.util.Log.e(
                        "SecureNoteRepository",
                        "Failed to decrypt note " + id,
                        e
                );

                mainHandler.post(() -> callback.onResult(null));
            }
        });
    }

    private List<DecryptedNote> decryptList(
            List<SecureNoteEntity> entities
    ) {
        List<DecryptedNote> result = new ArrayList<>();

        for (SecureNoteEntity entity : entities) {
            try {
                String content =
                        cryptoManager.decryptToString(entity.encryptedContent);

                result.add(new DecryptedNote(
                        entity.id,
                        entity.title,
                        content,
                        entity.categoryId,
                        entity.isFavorite,
                        entity.createdAt,
                        entity.updatedAt
                ));

            } catch (Exception e) {
                android.util.Log.e(
                        "SecureNoteRepository",
                        "Failed to decrypt note " + entity.id,
                        e
                );
            }
        }

        return result;
    }

    public void toggleFavorite(int id, boolean isFavorite) {
        executorService.execute(
                () -> noteDao.updateFavoriteStatus(id, isFavorite)
        );
    }
}