package com.example.securedigitalvault.ui.photos;

/**
 * DecryptedPhoto
 *
 * Holds a photo's metadata plus its decrypted image bytes, ready to
 * be decoded into a Bitmap for display. imageBytes is only ever held
 * in memory for as long as needed to render the screen — never
 * written back to disk.
 */
public class DecryptedPhoto {

    public int id;
    public String displayName;
    public long dateAdded;
    public byte[] imageBytes;

    public DecryptedPhoto(int id, String displayName, long dateAdded, byte[] imageBytes) {
        this.id = id;
        this.displayName = displayName;
        this.dateAdded = dateAdded;
        this.imageBytes = imageBytes;
    }
}


