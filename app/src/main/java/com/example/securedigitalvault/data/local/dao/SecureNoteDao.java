package com.example.securedigitalvault.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.securedigitalvault.data.local.entity.SecureNoteEntity;

import java.util.List;

@Dao
public interface SecureNoteDao {

    @Insert
    long insert(SecureNoteEntity note);

    @Update
    void update(SecureNoteEntity note);

    @Delete
    void delete(SecureNoteEntity note);

    @Query("SELECT * FROM secure_notes ORDER BY updated_at DESC")
    LiveData<List<SecureNoteEntity>> getAllNotes();

    @Query("SELECT * FROM secure_notes WHERE id = :id LIMIT 1")
    SecureNoteEntity getNoteById(int id);

    @Query("SELECT COUNT(*) FROM secure_notes")
    LiveData<Integer> getNoteCountLive();

    @Query("UPDATE secure_notes SET is_favorite = :isFavorite WHERE id = :id")
    void updateFavoriteStatus(int id, boolean isFavorite);
}


