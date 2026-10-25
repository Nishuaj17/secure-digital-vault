package com.example.securedigitalvault.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.securedigitalvault.data.local.entity.PasswordEntity;

import java.util.List;

/**
 * PasswordDao
 *
 * Handles all database operations for password entries.
 *
 * SEARCH DESIGN NOTE: getBySearchTerm() only searches the "site_name"
 * column, which is stored as plain text. It does NOT search inside
 * encrypted_username or encrypted_password, since those are opaque
 * ciphertext blobs that SQL cannot meaningfully search anyway. This
 * matches the app's overall design principle: search only touches
 * non-sensitive metadata, never decrypted sensitive fields, to avoid
 * unnecessary bulk-decryption during a simple search operation.
 */
@Dao
public interface PasswordDao {

    @Insert
    long insert(PasswordEntity password);

    @Update
    void update(PasswordEntity password);

    @Delete
    void delete(PasswordEntity password);

    @Query("SELECT * FROM passwords ORDER BY updated_at DESC")
    LiveData<List<PasswordEntity>> getAllPasswords();

    @Query("SELECT * FROM passwords WHERE id = :id LIMIT 1")
    PasswordEntity getPasswordById(int id);

    @Query("SELECT * FROM passwords WHERE site_name LIKE '%' || :searchTerm || '%' ORDER BY updated_at DESC")
    LiveData<List<PasswordEntity>> searchBySiteName(String searchTerm);

    @Query("SELECT * FROM passwords WHERE category_id = :categoryId ORDER BY updated_at DESC")
    LiveData<List<PasswordEntity>> getByCategory(int categoryId);

    @Query("SELECT * FROM passwords WHERE is_favorite = 1 ORDER BY updated_at DESC")
    LiveData<List<PasswordEntity>> getFavorites();

    @Query("SELECT COUNT(*) FROM passwords")
    int getPasswordCount();

    @Query("SELECT COUNT(*) FROM passwords")
    LiveData<Integer> getPasswordCountLive();

    @Query("UPDATE passwords SET is_favorite = :isFavorite WHERE id = :id")
    void updateFavoriteStatus(int id, boolean isFavorite);
}


