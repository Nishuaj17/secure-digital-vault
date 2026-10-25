package com.example.securedigitalvault.ui.search;

/**
 * GlobalSearchResult
 *
 * A unified representation of a search hit from any module. Built
 * directly from each entity's PLAIN (non-sensitive) fields only —
 * title/name/nickname/service-name — never from decrypted content.
 * No CryptoManager involvement anywhere in the search path.
 */
public class GlobalSearchResult {

    public enum ResultType {
        PASSWORD, NOTE, DOCUMENT_ID, CARD, RECOVERY_CODE
    }

    public int entityId;
    public String title;
    public ResultType type;

    public GlobalSearchResult(int entityId, String title, ResultType type) {
        this.entityId = entityId;
        this.title = title;
        this.type = type;
    }

    public String getTypeLabel() {
        switch (type) {
            case PASSWORD: return "Password";
            case NOTE: return "Secure Note";
            case DOCUMENT_ID: return "ID / Document";
            case CARD: return "Card";
            case RECOVERY_CODE: return "Recovery Codes";
            default: return "";
        }
    }

    public int getIconResId() {
        switch (type) {
            case PASSWORD: return com.example.securedigitalvault.R.drawable.ic_password;
            case NOTE: return com.example.securedigitalvault.R.drawable.ic_note;
            case DOCUMENT_ID: return com.example.securedigitalvault.R.drawable.ic_id;
            case CARD: return com.example.securedigitalvault.R.drawable.ic_card;
            case RECOVERY_CODE: return com.example.securedigitalvault.R.drawable.ic_recovery;
            default: return com.example.securedigitalvault.R.drawable.ic_password;
        }
    }
}


