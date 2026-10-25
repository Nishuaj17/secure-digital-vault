package com.example.securedigitalvault.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.securedigitalvault.data.local.entity.DocumentFileEntity;

import java.util.List;

@Dao
public interface DocumentFileDao {

    @Insert
    long insert(DocumentFileEntity document);

    @Delete
    void delete(DocumentFileEntity document);

    @Query("SELECT * FROM document_files ORDER BY date_added DESC")
    LiveData<List<DocumentFileEntity>> getAllDocuments();

    @Query("SELECT * FROM document_files WHERE file_name LIKE '%' || :searchTerm || '%' ORDER BY date_added DESC")
    LiveData<List<DocumentFileEntity>> searchByFileName(String searchTerm);

    @Query("SELECT * FROM document_files WHERE id = :id LIMIT 1")
    DocumentFileEntity getDocumentById(int id);

    @Query("SELECT COUNT(*) FROM document_files")
    LiveData<Integer> getDocumentCountLive();
}

