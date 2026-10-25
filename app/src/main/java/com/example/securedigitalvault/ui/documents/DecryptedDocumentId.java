package com.example.securedigitalvault.ui.documents;

public class DecryptedDocumentId {

    public int id;
    public String documentName;
    public String documentType;
    public String documentNumber;
    public String issueDate;
    public String expiryDate;
    public String notes;
    public int categoryId;
    public boolean isFavorite;
    public long createdAt;
    public long updatedAt;

    public DecryptedDocumentId(int id, String documentName, String documentType,
                               String documentNumber, String issueDate, String expiryDate,
                               String notes, int categoryId, boolean isFavorite,
                               long createdAt, long updatedAt) {
        this.id = id;
        this.documentName = documentName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.notes = notes;
        this.categoryId = categoryId;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}


