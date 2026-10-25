package com.example.securedigitalvault.ui.cards;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.repository.CardRepository;
import com.example.securedigitalvault.util.SecurityUtils;

public class AddEditCardActivity extends AppCompatActivity {

    public static final String EXTRA_CARD_ID = "card_id";
    private static final int NO_ID = -1;

    private TextView screenTitle;
    private EditText nicknameInput, cardTypeInput, cardholderInput, cardNumberInput, expiryInput, notesInput;
    private TextView errorText;

    private CardRepository cardRepository;
    private int editingId = NO_ID;
    private long originalCreatedAt = 0;
    private boolean originalIsFavorite = false;
    private int originalCategoryId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurityUtils.applySecureFlag(this);
        setContentView(R.layout.activity_add_edit_card);

        cardRepository = new CardRepository(getApplicationContext());

        screenTitle = findViewById(R.id.screenTitle);
        nicknameInput = findViewById(R.id.nicknameInput);
        cardTypeInput = findViewById(R.id.cardTypeInput);
        cardholderInput = findViewById(R.id.cardholderInput);
        cardNumberInput = findViewById(R.id.cardNumberInput);
        expiryInput = findViewById(R.id.expiryInput);
        notesInput = findViewById(R.id.notesInput);
        errorText = findViewById(R.id.errorText);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.saveButton).setOnClickListener(v -> handleSaveClicked());

        editingId = getIntent().getIntExtra(EXTRA_CARD_ID, NO_ID);
        if (editingId != NO_ID) {
            loadExisting(editingId);
        }
    }

    private void loadExisting(int id) {
        screenTitle.setText("Edit Card");
        ((Button) findViewById(R.id.saveButton)).setText("Update Card");

        cardRepository.getCardByIdAsync(id, decrypted -> {
            if (decrypted == null) {
                Toast.makeText(this, "Could not load this card.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            nicknameInput.setText(decrypted.nickname);
            cardTypeInput.setText(decrypted.cardType);
            cardholderInput.setText(decrypted.cardholderName);
            cardNumberInput.setText(decrypted.cardNumber);
            expiryInput.setText(decrypted.expiry);
            notesInput.setText(decrypted.notes);
            originalCreatedAt = decrypted.createdAt;
            originalIsFavorite = decrypted.isFavorite;
            originalCategoryId = decrypted.categoryId;
        });
    }

    private void handleSaveClicked() {
        String nickname = nicknameInput.getText().toString().trim();
        String cardType = cardTypeInput.getText().toString().trim();
        String cardholder = cardholderInput.getText().toString().trim();
        String cardNumber = cardNumberInput.getText().toString().trim();
        String expiry = expiryInput.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();

        if (nickname.isEmpty()) {
            showError("Please enter a card nickname.");
            return;
        }
        String digitsOnly = cardNumber.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 4) {
            showError("Please enter a valid card number.");
            return;
        }

        if (editingId == NO_ID) {
            cardRepository.insertCard(nickname, cardType, cardholder, cardNumber, expiry, notes, 0, false);
            Toast.makeText(this, "Card saved", Toast.LENGTH_SHORT).show();
        } else {
            DecryptedCard updated = new DecryptedCard(editingId, nickname, cardType,
                    "", cardholder, cardNumber, expiry, notes, originalCategoryId,
                    originalIsFavorite, originalCreatedAt, System.currentTimeMillis());
            cardRepository.updateCard(updated);
            Toast.makeText(this, "Card updated", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}


