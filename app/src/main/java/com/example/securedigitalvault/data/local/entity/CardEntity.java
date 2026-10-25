package com.example.securedigitalvault.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * CardEntity
 *
 * nickname, cardType, lastFourDigits: plain columns. lastFourDigits is
 * intentionally NOT encrypted — it's what gets shown in the masked
 * "**** **** **** 1234" display, and four digits alone are not
 * sufficient to reconstruct a usable card number, so this matches the
 * spec's classification of "last four digits" as non-sensitive
 * metadata.
 *
 * encryptedCardholderName, encryptedCardNumber, encryptedExpiry,
 * encryptedNotes: sensitive fields, always encrypted.
 *
 * DELIBERATELY OMITTED: CVV. Per the security spec, CVVs are not
 * stored in this app. A CVV is meant to be a point-in-time proof of
 * physical card possession, not something a password-manager-style
 * vault should retain — storing it would meaningfully increase the
 * damage potential of any future compromise for very little
 * legitimate benefit to the user.
 */
@Entity(tableName = "cards")
public class CardEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "nickname")
    public String nickname;

    @ColumnInfo(name = "card_type")
    public String cardType;

    @ColumnInfo(name = "last_four_digits")
    public String lastFourDigits;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "is_favorite")
    public boolean isFavorite;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "encrypted_cardholder_name")
    public byte[] encryptedCardholderName;

    @ColumnInfo(name = "encrypted_card_number")
    public byte[] encryptedCardNumber;

    @ColumnInfo(name = "encrypted_expiry")
    public byte[] encryptedExpiry;

    @ColumnInfo(name = "encrypted_notes")
    public byte[] encryptedNotes;

    public CardEntity(String nickname, String cardType, String lastFourDigits, int categoryId,
                      boolean isFavorite, long createdAt, long updatedAt,
                      byte[] encryptedCardholderName, byte[] encryptedCardNumber,
                      byte[] encryptedExpiry, byte[] encryptedNotes) {
        this.nickname = nickname;
        this.cardType = cardType;
        this.lastFourDigits = lastFourDigits;
        this.categoryId = categoryId;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.encryptedCardholderName = encryptedCardholderName;
        this.encryptedCardNumber = encryptedCardNumber;
        this.encryptedExpiry = encryptedExpiry;
        this.encryptedNotes = encryptedNotes;
    }
}


