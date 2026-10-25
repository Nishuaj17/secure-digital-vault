package com.example.securedigitalvault.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.securedigitalvault.data.local.entity.RecoveryCodeEntity;

import java.util.List;

@Dao
public interface RecoveryCodeDao {

    @Insert
    long insert(RecoveryCodeEntity entry);

    @Update
    void update(RecoveryCodeEntity entry);

    @Delete
    void delete(RecoveryCodeEntity entry);

    @Query("SELECT * FROM recovery_codes ORDER BY updated_at DESC")
    LiveData<List<RecoveryCodeEntity>> getAllEntries();

    @Query("SELECT * FROM recovery_codes WHERE id = :id LIMIT 1")
    RecoveryCodeEntity getEntryById(int id);

    @Query("SELECT COUNT(*) FROM recovery_codes")
    LiveData<Integer> getEntryCountLive();

    @Query("UPDATE recovery_codes SET is_favorite = :isFavorite WHERE id = :id")
    void updateFavoriteStatus(int id, boolean isFavorite);
}


