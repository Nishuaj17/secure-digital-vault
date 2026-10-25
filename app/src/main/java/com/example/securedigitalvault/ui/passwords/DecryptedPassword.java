package com.example.securedigitalvault.ui.passwords;

/**
 * DecryptedPassword
 *
 * A plaintext, UI-ready representation of a password entry. This is
 * what Activities/Fragments actually work with — never PasswordEntity
 * directly, which holds encrypted byte[] blobs.
 *
 * SECURITY NOTE: Instances of this class hold decrypted sensitive data
 * in memory (username, password, notes) for as long as they're needed
 * to render a screen. They are never persisted anywhere themselves —
 * only PasswordRepository writes to the database, and it always
 * re-encrypts before doing so. Avoid holding onto DecryptedPassword
 * instances longer than necessary (e.g. don't cache them in a static
 * field), and never pass one through an Intent extra.
 */
public class DecryptedPassword {

    public int id;
    public String siteName;
    public String username;
    public String password;
    public String url;
    public String notes;
    public int categoryId;
    public boolean isFavorite;
    public long createdAt;
    public long updatedAt;

    public DecryptedPassword(int id, String siteName, String username, String password,
                             String url, String notes, int categoryId, boolean isFavorite,
                             long createdAt, long updatedAt) {
        this.id = id;
        this.siteName = siteName;
        this.username = username;
        this.password = password;
        this.url = url;
        this.notes = notes;
        this.categoryId = categoryId;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}


