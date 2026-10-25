package com.example.securedigitalvault.ui.documents;

public class DecryptedDocumentFile {

    public int id;
    public String fileName;
    public String fileType;
    public long fileSizeBytes;
    public String encryptedFileName;
    public long dateAdded;

    public DecryptedDocumentFile(int id, String fileName, String fileType, long fileSizeBytes,
                                 String encryptedFileName, long dateAdded) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSizeBytes = fileSizeBytes;
        this.encryptedFileName = encryptedFileName;
        this.dateAdded = dateAdded;
    }

    public String getReadableSize() {
        if (fileSizeBytes < 1024) return fileSizeBytes + " B";
        if (fileSizeBytes < 1024 * 1024) return (fileSizeBytes / 1024) + " KB";
        return (fileSizeBytes / (1024 * 1024)) + " MB";
    }
}

