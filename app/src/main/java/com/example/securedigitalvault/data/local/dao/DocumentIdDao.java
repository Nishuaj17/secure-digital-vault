package com.example.securedigitalvault.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.securedigitalvault.data.local.entity.DocumentIdEntity;

import java.util.List;

@Dao
public interface DocumentIdDao {

    @Insert
    long insert(DocumentIdEntity document);

    @Update
    void update(DocumentIdEntity document);

    @Delete
    void delete(DocumentIdEntity document);

    @Query("SELECT * FROM document_ids ORDER BY updated_at DESC")
    LiveData<List<DocumentIdEntity>> getAllDocuments();

    @Query("SELECT * FROM document_ids WHERE id = :id LIMIT 1")
    DocumentIdEntity getDocumentById(int id);

    @Query("SELECT COUNT(*) FROM document_ids")
    LiveData<Integer> getDocumentCountLive();

    @Query("UPDATE document_ids SET is_favorite = :isFavorite WHERE id = :id")
    void updateFavoriteStatus(int id, boolean isFavorite);
}


