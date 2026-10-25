package com.example.securedigitalvault.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.securedigitalvault.data.local.dao.CategoryDao;
import com.example.securedigitalvault.data.local.dao.PasswordDao;
import com.example.securedigitalvault.data.local.entity.CategoryEntity;
import com.example.securedigitalvault.data.local.entity.PasswordEntity;
import com.example.securedigitalvault.data.local.dao.SecureNoteDao;
import com.example.securedigitalvault.data.local.entity.SecureNoteEntity;
import com.example.securedigitalvault.data.local.dao.DocumentIdDao;
import com.example.securedigitalvault.data.local.entity.DocumentIdEntity;
import com.example.securedigitalvault.data.local.dao.CardDao;
import com.example.securedigitalvault.data.local.dao.RecoveryCodeDao;
import com.example.securedigitalvault.data.local.entity.CardEntity;
import com.example.securedigitalvault.data.local.entity.RecoveryCodeEntity;
import com.example.securedigitalvault.data.local.dao.PhotoDao;
import com.example.securedigitalvault.data.local.entity.PhotoEntity;
import com.example.securedigitalvault.data.local.dao.DocumentFileDao;
import com.example.securedigitalvault.data.local.entity.DocumentFileEntity;

/**
 * VaultDatabase
 *
 * The single Room database for the entire app. Uses a singleton
 * pattern (getInstance) so only one database connection exists at a
 * time, shared across all repositories.
 *
 * As we build out more modules (Secure Notes, IDs, Cards, Photos,
 * PDFs, Recovery Codes) in later phases, their entities and DAOs will
 * be added to the @Database annotation's "entities" array here, and
 * the database version will be incremented with an appropriate
 * migration — we are NOT using fallbackToDestructiveMigration in the
 * final app, since that would silently delete all user data on a
 * schema change. For now, during early development, we allow it for
 * convenience since there is no real user data yet to protect.
 */
@Database(entities = {PasswordEntity.class, CategoryEntity.class, SecureNoteEntity.class, DocumentIdEntity.class, CardEntity.class, RecoveryCodeEntity.class, PhotoEntity.class, DocumentFileEntity.class}, version = 7, exportSchema = false)public abstract class VaultDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "secure_vault_database";
    private static volatile VaultDatabase instance;

    public abstract PasswordDao passwordDao();

    public abstract CategoryDao categoryDao();
    public abstract SecureNoteDao secureNoteDao();
    public abstract DocumentIdDao documentIdDao();
    public abstract CardDao cardDao();
    public abstract RecoveryCodeDao recoveryCodeDao();
    public abstract PhotoDao photoDao();
    public abstract DocumentFileDao documentFileDao();

    public static VaultDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (VaultDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    VaultDatabase.class,
                                    DATABASE_NAME)
                            // TEMPORARY during early development only.
                            // TODO: remove this before real user data exists,
                            // and replace with a proper Migration() once the
                            // schema stabilizes, so future schema changes
                            // don't silently wipe the user's vault.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}


