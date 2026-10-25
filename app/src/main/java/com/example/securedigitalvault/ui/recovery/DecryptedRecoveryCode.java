package com.example.securedigitalvault.ui.recovery;

public class DecryptedRecoveryCode {

    public int id;
    public String serviceName;
    public String codes;
    public String notes;
    public int categoryId;
    public boolean isFavorite;
    public long createdAt;
    public long updatedAt;

    public DecryptedRecoveryCode(int id, String serviceName, String codes, String notes,
                                 int categoryId, boolean isFavorite, long createdAt, long updatedAt) {
        this.id = id;
        this.serviceName = serviceName;
        this.codes = codes;
        this.notes = notes;
        this.categoryId = categoryId;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}


