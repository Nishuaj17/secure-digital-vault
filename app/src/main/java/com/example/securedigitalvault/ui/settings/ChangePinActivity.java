package com.example.securedigitalvault.ui.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.prefs.AppPreferences;
import com.example.securedigitalvault.security.PinHasher;
import com.example.securedigitalvault.util.SecurityUtils;

import java.util.Arrays;

/**
 * ChangePinActivity
 *
 * Three-stage flow:
 *   1. Verify the user's CURRENT PIN (proves it's really them before
 *      allowing a change).
 *   2. Enter a new PIN.
 *   3. Confirm the new PIN.
 *
 * SECURITY NOTE: We do NOT re-encrypt any vault data here. Since the
 * PIN is only used to gate access via a PBKDF2 hash (see PinHasher and
 * the Phase 3 architecture notes), and the actual AES data-encryption
 * key lives independently in the Android Keystore, changing the PIN
 * only requires updating the stored hash + salt — the vault's existing
 * encrypted data remains valid and untouched.
 */
public class ChangePinActivity extends AppCompatActivity {

    private static final int PIN_LENGTH = 6;
    private enum Stage { VERIFY_CURRENT, ENTER_NEW, CONFIRM_NEW }

    private TextView stageLabel;
    private TextView errorText;
    private EditText pinInput;
    private Button continueButton;

    private AppPreferences appPreferences;
    private Stage currentStage = Stage.VERIFY_CURRENT;
    private char[] newPinAttempt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurityUtils.applySecureFlag(this);
        setContentView(R.layout.activity_change_pin);

        appPreferences = new AppPreferences(getApplicationContext());

        stageLabel = findViewById(R.id.stageLabel);
        errorText = findViewById(R.id.errorText);
        pinInput = findViewById(R.id.pinInput);
        continueButton = findViewById(R.id.continueButton);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        continueButton.setOnClickListener(v -> handleContinueClicked());

        pinInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (errorText.getVisibility() == View.VISIBLE) errorText.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void handleContinueClicked() {
        String typedText = pinInput.getText().toString();
        if (typedText.length() != PIN_LENGTH) {
            showError("Please enter exactly " + PIN_LENGTH + " digits.");
            return;
        }
        char[] typedPin = typedText.toCharArray();

        switch (currentStage) {
            case VERIFY_CURRENT:
                verifyCurrentPin(typedPin);
                break;
            case ENTER_NEW:
                newPinAttempt = typedPin;
                currentStage = Stage.CONFIRM_NEW;
                stageLabel.setText("Confirm new PIN");
                continueButton.setText("Confirm");
                pinInput.setText("");
                break;
            case CONFIRM_NEW:
                confirmNewPin(typedPin);
                break;
        }
    }

    private void verifyCurrentPin(char[] typedPin) {
        String storedHash = appPreferences.getPinHash();
        byte[] salt = PinHasher.decodeSalt(appPreferences.getPinSalt());
        boolean isCorrect = PinHasher.verifyPin(typedPin, storedHash, salt);

        if (isCorrect) {
            currentStage = Stage.ENTER_NEW;
            stageLabel.setText("Enter new PIN");
            continueButton.setText("Continue");
            pinInput.setText("");
        } else {
            showError("That PIN is incorrect.");
        }
        // Note: typedPin is already wiped inside PinHasher.hashPin(),
        // which verifyPin() calls internally.
    }

    private void confirmNewPin(char[] confirmedPin) {
        boolean pinsMatch = Arrays.equals(newPinAttempt, confirmedPin);

        if (pinsMatch) {
            byte[] newSalt = PinHasher.generateSalt();
            String newHash = PinHasher.hashPin(confirmedPin, newSalt);
            appPreferences.savePinHash(newHash, PinHasher.encodeSalt(newSalt));

            Toast.makeText(this, "PIN changed successfully", Toast.LENGTH_SHORT).show();
            wipeAndClearNewPinAttempt();
            finish();
        } else {
            showError("PINs don't match. Please start over.");
            Arrays.fill(confirmedPin, '\u0000');
            wipeAndClearNewPinAttempt();

            currentStage = Stage.ENTER_NEW;
            stageLabel.setText("Enter new PIN");
            continueButton.setText("Continue");
            pinInput.setText("");
        }
    }

    private void wipeAndClearNewPinAttempt() {
        if (newPinAttempt != null) {
            Arrays.fill(newPinAttempt, '\u0000');
            newPinAttempt = null;
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
        pinInput.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wipeAndClearNewPinAttempt();
    }
}


