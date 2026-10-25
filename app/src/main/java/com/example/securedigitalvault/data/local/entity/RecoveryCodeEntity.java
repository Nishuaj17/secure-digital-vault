package com.example.securedigitalvault.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * RecoveryCodeEntity
 *
 * serviceName: plain column (non-sensitive, searchable — e.g. "Google",
 * "GitHub").
 * encryptedCodes: byte[] blob holding the actual recovery codes text
 * (often multiple codes, stored as one newline-separated string before
 * encryption).
 * encryptedNotes: byte[] blob for optional notes.
 */
@Entity(tableName = "recovery_codes")
public class RecoveryCodeEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "service_name")
    public String serviceName;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "is_favorite")
    public boolean isFavorite;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "encrypted_codes")
    public byte[] encryptedCodes;

    @ColumnInfo(name = "encrypted_notes")
    public byte[] encryptedNotes;

    public RecoveryCodeEntity(String serviceName, int categoryId, boolean isFavorite,
                              long createdAt, long updatedAt, byte[] encryptedCodes,
                              byte[] encryptedNotes) {
        this.serviceName = serviceName;
        this.categoryId = categoryId;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.encryptedCodes = encryptedCodes;
        this.encryptedNotes = encryptedNotes;
    }
}


