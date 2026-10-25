package com.example.securedigitalvault.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * SecureNoteEntity
 *
 * title: plain column (non-sensitive, searchable metadata).
 * encryptedContent: byte[] blob from CryptoManager.encrypt() — the
 * actual note text, never stored as plain text.
 */
@Entity(tableName = "secure_notes")
public class SecureNoteEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "is_favorite")
    public boolean isFavorite;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "encrypted_content")
    public byte[] encryptedContent;

    public SecureNoteEntity(String title, int categoryId, boolean isFavorite,
                            long createdAt, long updatedAt, byte[] encryptedContent) {
        this.title = title;
        this.categoryId = categoryId;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.encryptedContent = encryptedContent;
    }
}

