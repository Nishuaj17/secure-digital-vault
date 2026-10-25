package com.example.securedigitalvault.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * PasswordEntity
 *
 * Represents one saved password entry.
 *
 * FIELD SENSITIVITY:
 * - siteName, url, categoryId, isFavorite, createdAt, updatedAt:
 *   plain columns (non-sensitive metadata, needed for search/sort/filter
 *   without decrypting anything).
 * - encryptedUsername, encryptedPassword, encryptedNotes: each stores
 *   a byte[] blob produced by CryptoManager.encrypt(), which already
 *   contains [IV][ciphertext]. These are NEVER stored as plain String
 *   columns.
 *
 * Room stores byte[] columns as BLOB automatically — no TypeConverter
 * needed for this entity.
 */
@Entity(tableName = "passwords")
public class PasswordEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // Non-sensitive, searchable metadata.
    @ColumnInfo(name = "site_name")
    public String siteName;

    @ColumnInfo(name = "url")
    public String url;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "is_favorite")
    public boolean isFavorite;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    // Sensitive fields — always encrypted before being assigned here.
    // Never assign a plain String's raw bytes to these directly.
    @ColumnInfo(name = "encrypted_username")
    public byte[] encryptedUsername;

    @ColumnInfo(name = "encrypted_password")
    public byte[] encryptedPassword;

    @ColumnInfo(name = "encrypted_notes")
    public byte[] encryptedNotes;

    public PasswordEntity(String siteName, String url, int categoryId, boolean isFavorite,
                          long createdAt, long updatedAt, byte[] encryptedUsername,
                          byte[] encryptedPassword, byte[] encryptedNotes) {
        this.siteName = siteName;
        this.url = url;
        this.categoryId = categoryId;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.encryptedUsername = encryptedUsername;
        this.encryptedPassword = encryptedPassword;
        this.encryptedNotes = encryptedNotes;
    }
}

