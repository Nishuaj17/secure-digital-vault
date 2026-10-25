package com.example.securedigitalvault.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * PhotoEntity
 *
 * Stores only METADATA about a photo — never the image bytes
 * themselves. The actual encrypted image lives in a file on disk
 * (managed by SecureFileManager), referenced here by filename only.
 *
 * encryptedFileName: the generated filename (e.g. a UUID + ".enc")
 * inside the app's private vault_photos directory. This is just a
 * filename string, not sensitive on its own, and not encrypted itself
 * — the actual image content within that file is what's encrypted.
 */
@Entity(tableName = "photos")
public class PhotoEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "display_name")
    public String displayName;

    @ColumnInfo(name = "encrypted_file_name")
    public String encryptedFileName;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "date_added")
    public long dateAdded;

    public PhotoEntity(String displayName, String encryptedFileName, int categoryId, long dateAdded) {
        this.displayName = displayName;
        this.encryptedFileName = encryptedFileName;
        this.categoryId = categoryId;
        this.dateAdded = dateAdded;
    }
}


