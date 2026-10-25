package com.example.securedigitalvault.ui.notes;

public class DecryptedNote {

    public int id;
    public String title;
    public String content;
    public int categoryId;
    public boolean isFavorite;
    public long createdAt;
    public long updatedAt;

    public DecryptedNote(int id, String title, String content, int categoryId,
                         boolean isFavorite, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}


