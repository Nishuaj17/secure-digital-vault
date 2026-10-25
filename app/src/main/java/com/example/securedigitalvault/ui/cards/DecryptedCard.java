package com.example.securedigitalvault.ui.cards;

public class DecryptedCard {

    public int id;
    public String nickname;
    public String cardType;
    public String lastFourDigits;
    public String cardholderName;
    public String cardNumber;
    public String expiry;
    public String notes;
    public int categoryId;
    public boolean isFavorite;
    public long createdAt;
    public long updatedAt;

    public DecryptedCard(int id, String nickname, String cardType, String lastFourDigits,
                         String cardholderName, String cardNumber, String expiry, String notes,
                         int categoryId, boolean isFavorite, long createdAt, long updatedAt) {
        this.id = id;
        this.nickname = nickname;
        this.cardType = cardType;
        this.lastFourDigits = lastFourDigits;
        this.cardholderName = cardholderName;
        this.cardNumber = cardNumber;
        this.expiry = expiry;
        this.notes = notes;
        this.categoryId = categoryId;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the masked display format: **** **** **** 1234
     */
    public String getMaskedNumber() {
        return "•••• •••• •••• " + (lastFourDigits.isEmpty() ? "????" : lastFourDigits);
    }
}


