package com.example.securedigitalvault.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * DocumentFileEntity
 *
 * Stores metadata about an imported PDF/document. As with photos, the
 * actual file bytes are never stored in the database — only a
 * filename reference to the encrypted file in private storage.
 */
@Entity(tableName = "document_files")
public class DocumentFileEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "file_name")
    public String fileName;

    @ColumnInfo(name = "file_type")
    public String fileType;

    @ColumnInfo(name = "file_size_bytes")
    public long fileSizeBytes;

    @ColumnInfo(name = "encrypted_file_name")
    public String encryptedFileName;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "date_added")
    public long dateAdded;

    public DocumentFileEntity(String fileName, String fileType, long fileSizeBytes,
                              String encryptedFileName, int categoryId, long dateAdded) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSizeBytes = fileSizeBytes;
        this.encryptedFileName = encryptedFileName;
        this.categoryId = categoryId;
        this.dateAdded = dateAdded;
    }
}

