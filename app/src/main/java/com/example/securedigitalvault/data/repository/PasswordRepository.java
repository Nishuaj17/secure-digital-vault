
package com.example.securedigitalvault.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.securedigitalvault.data.local.VaultDatabase;
import com.example.securedigitalvault.data.local.dao.PasswordDao;
import com.example.securedigitalvault.data.local.entity.PasswordEntity;
import com.example.securedigitalvault.security.CryptoManager;
import com.example.securedigitalvault.security.KeystoreManager;
import com.example.securedigitalvault.ui.passwords.DecryptedPassword;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PasswordRepository {

    private final PasswordDao passwordDao;
    private final CryptoManager cryptoManager;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public PasswordRepository(Context context) {
        VaultDatabase database = VaultDatabase.getInstance(context);

        this.passwordDao = database.passwordDao();
        this.cryptoManager = new CryptoManager(new KeystoreManager());
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // Callback used to report whether saving succeeded or failed.
    public interface SaveCallback {
        void onSuccess();

        void onError();
    }

    // Returns the list of decrypted passwords for display.
    public LiveData<List<DecryptedPassword>> getAllPasswords() {
        return Transformations.map(
                passwordDao.getAllPasswords(),
                this::decryptList
        );
    }

    public LiveData<Integer> getPasswordCountLive() {
        return passwordDao.getPasswordCountLive();
    }

    // Encrypts sensitive fields and inserts a new password in the background.
    public void insertPassword(
            String siteName,
            String username,
            String password,
            String url,
            String notes,
            int categoryId,
            boolean isFavorite,
            SaveCallback callback) {

        executorService.execute(() -> {
            try {
                long now = System.currentTimeMillis();

                byte[] encryptedUsername =
                        cryptoManager.encryptString(username);

                byte[] encryptedPassword =
                        cryptoManager.encryptString(password);

                byte[] encryptedNotes =
                        cryptoManager.encryptString(
                                notes != null ? notes : ""
                        );

                PasswordEntity entity = new PasswordEntity(
                        siteName,
                        url,
                        categoryId,
                        isFavorite,
                        now,
                        now,
                        encryptedUsername,
                        encryptedPassword,
                        encryptedNotes
                );

                long insertedId = passwordDao.insert(entity);

                if (insertedId <= 0) {
                    throw new IllegalStateException(
                            "The password was not inserted into the database."
                    );
                }

                if (callback != null) {
                    mainHandler.post(callback::onSuccess);
                }

            } catch (Exception e) {
                android.util.Log.e(
                        "PasswordRepository",
                        "Failed to save password",
                        e
                );

                if (callback != null) {
                    mainHandler.post(callback::onError);
                }
            }
        });
    }

    // Encrypts and updates an existing password.
    public void updatePassword(DecryptedPassword decrypted) {
        executorService.execute(() -> {
            try {
                byte[] encryptedUsername =
                        cryptoManager.encryptString(decrypted.username);

                byte[] encryptedPassword =
                        cryptoManager.encryptString(decrypted.password);

                byte[] encryptedNotes =
                        cryptoManager.encryptString(
                                decrypted.notes != null
                                        ? decrypted.notes
                                        : ""
                        );

                PasswordEntity entity = new PasswordEntity(
                        decrypted.siteName,
                        decrypted.url,
                        decrypted.categoryId,
                        decrypted.isFavorite,
                        decrypted.createdAt,
                        System.currentTimeMillis(),
                        encryptedUsername,
                        encryptedPassword,
                        encryptedNotes
                );

                entity.id = decrypted.id;
                passwordDao.update(entity);

            } catch (Exception e) {
                // Update error handling can be added through a callback
                // if the UI needs to display update failures.
            }
        });
    }

    // Deletes a password entry by its ID.
    public void deletePassword(int id) {
        executorService.execute(() -> {
            PasswordEntity entity = passwordDao.getPasswordById(id);

            if (entity != null) {
                passwordDao.delete(entity);
            }
        });
    }

    public interface PasswordCallback {
        void onResult(DecryptedPassword password);
    }

    // Loads one password asynchronously and returns it on the main thread.
    public void getPasswordByIdAsync(int id, PasswordCallback callback) {
        executorService.execute(() -> {
            try {
                PasswordEntity entity = passwordDao.getPasswordById(id);

                if (entity == null) {
                    mainHandler.post(() -> callback.onResult(null));
                    return;
                }

                String username =
                        cryptoManager.decryptToString(entity.encryptedUsername);

                String password =
                        cryptoManager.decryptToString(entity.encryptedPassword);

                String notes =
                        cryptoManager.decryptToString(entity.encryptedNotes);

                DecryptedPassword decrypted = new DecryptedPassword(
                        entity.id,
                        entity.siteName,
                        username,
                        password,
                        entity.url,
                        notes,
                        entity.categoryId,
                        entity.isFavorite,
                        entity.createdAt,
                        entity.updatedAt
                );

                mainHandler.post(() -> callback.onResult(decrypted));

            } catch (Exception e) {
                // Report a load failure rather than crashing the worker thread.
                mainHandler.post(() -> callback.onResult(null));
            }
        });
    }

    // Converts database entities into decrypted objects for the UI.
    private List<DecryptedPassword> decryptList(
            List<PasswordEntity> entities) {

        List<DecryptedPassword> decryptedList = new ArrayList<>();

        for (PasswordEntity entity : entities) {
            try {
                String username =
                        cryptoManager.decryptToString(entity.encryptedUsername);

                String password =
                        cryptoManager.decryptToString(entity.encryptedPassword);

                String notes =
                        cryptoManager.decryptToString(entity.encryptedNotes);

                decryptedList.add(new DecryptedPassword(
                        entity.id,
                        entity.siteName,
                        username,
                        password,
                        entity.url,
                        notes,
                        entity.categoryId,
                        entity.isFavorite,
                        entity.createdAt,
                        entity.updatedAt
                ));

            } catch (Exception e) {
                // Skip an entry that cannot be decrypted.
                // Never display encrypted bytes as plaintext.
            }
        }

        return decryptedList;
    }

    // Updates the favorite status in the background.
    public void toggleFavorite(int id, boolean isFavorite) {
        executorService.execute(
                () -> passwordDao.updateFavoriteStatus(id, isFavorite)
        );
    }
}
