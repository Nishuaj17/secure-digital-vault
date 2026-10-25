package com.example.securedigitalvault.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * DocumentIdEntity
 *
 * Represents a personal identification record (passport, driver's
 * license, national ID, etc). Named "DocumentIdEntity" rather than
 * "DocumentEntity" to keep it distinct from the separate PDF/file
 * vault built in a later module.
 *
 * documentName, documentType, issueDate, expiryDate: plain columns
 * (non-sensitive metadata — dates and a label like "Passport" are not
 * meaningfully sensitive on their own).
 * encryptedDocumentNumber, encryptedNotes: byte[] blobs, the actual
 * sensitive identifying data.
 */
@Entity(tableName = "document_ids")
public class DocumentIdEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "document_name")
    public String documentName;

    @ColumnInfo(name = "document_type")
    public String documentType;

    @ColumnInfo(name = "issue_date")
    public String issueDate;

    @ColumnInfo(name = "expiry_date")
    public String expiryDate;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "is_favorite")
    public boolean isFavorite;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "encrypted_document_number")
    public byte[] encryptedDocumentNumber;

    @ColumnInfo(name = "encrypted_notes")
    public byte[] encryptedNotes;

    public DocumentIdEntity(String documentName, String documentType, String issueDate,
                            String expiryDate, int categoryId, boolean isFavorite,
                            long createdAt, long updatedAt, byte[] encryptedDocumentNumber,
                            byte[] encryptedNotes) {
        this.documentName = documentName;
        this.documentType = documentType;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.categoryId = categoryId;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.encryptedDocumentNumber = encryptedDocumentNumber;
        this.encryptedNotes = encryptedNotes;
    }
}


