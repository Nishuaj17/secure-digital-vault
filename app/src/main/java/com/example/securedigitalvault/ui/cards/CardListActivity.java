package com.example.securedigitalvault.ui.cards;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.prefs.AppPreferences;
import com.example.securedigitalvault.data.repository.CardRepository;

/**
 * CardListActivity
 *
 * Shows all saved cards with masked numbers. "Show Details" triggers
 * a biometric re-check (if the user has biometric enabled and the
 * device supports it) before revealing the full number. This is a
 * lightweight re-verification step for an especially sensitive field,
 * separate from the app-level lock screen the user already passed
 * to get here.
 */
public class CardListActivity extends AppCompatActivity implements CardListAdapter.OnCardActionListener {

    private RecyclerView recyclerView;
    private android.widget.TextView emptyStateText;
    private CardRepository cardRepository;
    private CardListAdapter adapter;
    private AppPreferences appPreferences;
    private BiometricManager biometricManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        cardRepository = new CardRepository(getApplicationContext());
        appPreferences = new AppPreferences(getApplicationContext());
        biometricManager = BiometricManager.from(this);

        recyclerView = findViewById(R.id.cardRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        adapter = new CardListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.addButton).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditCardActivity.class)));

        cardRepository.getAllCards().observe(this, cards -> {
            adapter.submitList(cards);
            emptyStateText.setVisibility(cards.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(cards.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onShowDetailsRequested(DecryptedCard card, Runnable onApproved) {
        boolean biometricEnabled = appPreferences.isBiometricEnabled();
        boolean biometricAvailable = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS;

        if (!biometricEnabled || !biometricAvailable) {
            // No biometric configured — the user already authenticated
            // to enter the app at all, so we allow the reveal directly.
            onApproved.run();
            return;
        }

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirm it's you")
                .setSubtitle("Verify to view full card details")
                .setNegativeButtonText("Cancel")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        onApproved.run();
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    public void onFavoriteToggle(DecryptedCard card) {
        cardRepository.toggleFavorite(card.id, !card.isFavorite);
    }

    @Override
    public void onDeleteClick(DecryptedCard card) {
        new AlertDialog.Builder(this)
                .setTitle("Delete this card?")
                .setMessage("This will permanently delete \"" + card.nickname + "\". This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> cardRepository.deleteCard(card.id))
                .setNegativeButton("Cancel", null)
                .show();
    }
}

