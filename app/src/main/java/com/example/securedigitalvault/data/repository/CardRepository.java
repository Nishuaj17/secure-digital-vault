package com.example.securedigitalvault.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.securedigitalvault.data.local.VaultDatabase;
import com.example.securedigitalvault.data.local.dao.CardDao;
import com.example.securedigitalvault.data.local.entity.CardEntity;
import com.example.securedigitalvault.security.CryptoManager;
import com.example.securedigitalvault.security.KeystoreManager;
import com.example.securedigitalvault.ui.cards.DecryptedCard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CardRepository {

    public interface CardCallback {
        void onResult(DecryptedCard card);
    }

    private final CardDao cardDao;
    private final CryptoManager cryptoManager;
    private final ExecutorService executorService;

    public CardRepository(Context context) {
        VaultDatabase database = VaultDatabase.getInstance(context);
        this.cardDao = database.cardDao();
        this.cryptoManager = new CryptoManager(new KeystoreManager());
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<DecryptedCard>> getAllCards() {
        return Transformations.map(cardDao.getAllCards(), this::decryptList);
    }

    public LiveData<Integer> getCardCountLive() {
        return cardDao.getCardCountLive();
    }

    public void insertCard(String nickname, String cardType, String cardholderName,
                           String fullCardNumber, String expiry, String notes,
                           int categoryId, boolean isFavorite) {
        executorService.execute(() -> {
            long now = System.currentTimeMillis();
            String lastFour = extractLastFour(fullCardNumber);

            byte[] encryptedName = cryptoManager.encryptString(cardholderName);
            byte[] encryptedNumber = cryptoManager.encryptString(fullCardNumber);
            byte[] encryptedExpiry = cryptoManager.encryptString(expiry);
            byte[] encryptedNotes = cryptoManager.encryptString(notes != null ? notes : "");

            CardEntity entity = new CardEntity(nickname, cardType, lastFour, categoryId,
                    isFavorite, now, now, encryptedName, encryptedNumber, encryptedExpiry, encryptedNotes);
            cardDao.insert(entity);
        });
    }

    public void updateCard(DecryptedCard decrypted) {
        executorService.execute(() -> {
            String lastFour = extractLastFour(decrypted.cardNumber);

            byte[] encryptedName = cryptoManager.encryptString(decrypted.cardholderName);
            byte[] encryptedNumber = cryptoManager.encryptString(decrypted.cardNumber);
            byte[] encryptedExpiry = cryptoManager.encryptString(decrypted.expiry);
            byte[] encryptedNotes = cryptoManager.encryptString(
                    decrypted.notes != null ? decrypted.notes : "");

            CardEntity entity = new CardEntity(decrypted.nickname, decrypted.cardType, lastFour,
                    decrypted.categoryId, decrypted.isFavorite, decrypted.createdAt,
                    System.currentTimeMillis(), encryptedName, encryptedNumber, encryptedExpiry, encryptedNotes);
            entity.id = decrypted.id;
            cardDao.update(entity);
        });
    }

    public void deleteCard(int id) {
        executorService.execute(() -> {
            CardEntity entity = cardDao.getCardById(id);
            if (entity != null) {
                cardDao.delete(entity);
            }
        });
    }

    public void getCardByIdAsync(int id, CardCallback callback) {
        executorService.execute(() -> {
            CardEntity entity = cardDao.getCardById(id);
            Handler mainHandler = new Handler(Looper.getMainLooper());

            if (entity == null) {
                mainHandler.post(() -> callback.onResult(null));
                return;
            }

            DecryptedCard decrypted = decryptEntity(entity);
            mainHandler.post(() -> callback.onResult(decrypted));
        });
    }

    private DecryptedCard decryptEntity(CardEntity entity) {
        String name = cryptoManager.decryptToString(entity.encryptedCardholderName);
        String number = cryptoManager.decryptToString(entity.encryptedCardNumber);
        String expiry = cryptoManager.decryptToString(entity.encryptedExpiry);
        String notes = cryptoManager.decryptToString(entity.encryptedNotes);

        return new DecryptedCard(entity.id, entity.nickname, entity.cardType,
                entity.lastFourDigits, name, number, expiry, notes, entity.categoryId,
                entity.isFavorite, entity.createdAt, entity.updatedAt);
    }

    private List<DecryptedCard> decryptList(List<CardEntity> entities) {
        List<DecryptedCard> result = new ArrayList<>();
        for (CardEntity entity : entities) {
            result.add(decryptEntity(entity));
        }
        return result;
    }

    private String extractLastFour(String fullNumber) {
        String digitsOnly = fullNumber.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 4) {
            return digitsOnly;
        }
        return digitsOnly.substring(digitsOnly.length() - 4);
    }
    public void toggleFavorite(int id, boolean isFavorite) {
        executorService.execute(() -> cardDao.updateFavoriteStatus(id, isFavorite));
    }
}


