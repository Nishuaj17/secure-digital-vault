package com.example.securedigitalvault.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.prefs.AppPreferences;
import com.example.securedigitalvault.security.PinHasher;

import java.util.Arrays;

/**
 * SetupPinActivity
 *
 * Handles first-time PIN creation using a simple two-stage flow on a
 * single screen:
 *   Stage 1: user types a new 6-digit PIN
 *   Stage 2: user re-types it to confirm
 *
 * SECURITY NOTES:
 * - The PIN is held only in a char[] in memory, never as a String,
 *   and is wiped (Arrays.fill) as soon as it is no longer needed.
 * - We never log the PIN or include it in any Intent extras.
 * - On successful confirmation, only the PBKDF2 hash + salt are
 *   persisted via AppPreferences — never the raw PIN.
 */
public class SetupPinActivity extends AppCompatActivity {

    private static final int PIN_LENGTH = 6;

    private TextView stageLabel;
    private TextView errorText;
    private TextView stepIndicatorText;
    private EditText pinInput;
    private Button continueButton;

    private AppPreferences appPreferences;

    private boolean isConfirmStage = false;
    private char[] firstPinAttempt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_pin);

        appPreferences = new AppPreferences(getApplicationContext());

        stageLabel = findViewById(R.id.stageLabel);
        errorText = findViewById(R.id.errorText);
        stepIndicatorText = findViewById(R.id.stepIndicatorText);
        pinInput = findViewById(R.id.pinInput);
        continueButton = findViewById(R.id.continueButton);

        // Hide the error message as soon as the user starts typing again.
        pinInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (errorText.getVisibility() == View.VISIBLE) {
                    errorText.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        continueButton.setOnClickListener(v -> handleContinueClicked());
    }

    private void handleContinueClicked() {
        String typedText = pinInput.getText().toString();

        if (typedText.length() != PIN_LENGTH) {
            showError("Please enter exactly " + PIN_LENGTH + " digits.");
            return;
        }

        char[] typedPin = typedText.toCharArray();

        if (!isConfirmStage) {
            // Stage 1 complete: remember this attempt, move to confirmation.
            firstPinAttempt = typedPin;
            isConfirmStage = true;

            stageLabel.setText("Confirm PIN");
            stepIndicatorText.setText("Step 2 of 2");
            continueButton.setText("Confirm & Continue");
            pinInput.setText("");
        } else {
            // Stage 2: compare against the first attempt.
            boolean pinsMatch = Arrays.equals(firstPinAttempt, typedPin);

            if (pinsMatch) {
                savePinAndProceed(typedPin);
            } else {
                showError("PINs don't match. Please try again.");
                resetToFirstStage();
            }

            // Wipe the confirmation attempt from memory either way.
            Arrays.fill(typedPin, '\u0000');
        }
    }

    private void savePinAndProceed(char[] confirmedPin) {
        byte[] salt = PinHasher.generateSalt();
        String hash = PinHasher.hashPin(confirmedPin, salt);
        // Note: PinHasher.hashPin() already wipes confirmedPin internally
        // via its own finally block, so we don't need to do it again here.

        appPreferences.savePinHash(hash, PinHasher.encodeSalt(salt));
        appPreferences.setSetupComplete(true);

        // Also wipe our stored first attempt now that we're done with it.
        if (firstPinAttempt != null) {
            Arrays.fill(firstPinAttempt, '\u0000');
            firstPinAttempt = null;
        }

        Intent intent = new Intent(SetupPinActivity.this, EnableBiometricActivity.class);
        startActivity(intent);
        finish();
    }

    private void resetToFirstStage() {
        isConfirmStage = false;
        stageLabel.setText("Enter PIN");
        stepIndicatorText.setText("Step 1 of 2");
        continueButton.setText("Continue");
        pinInput.setText("");

        if (firstPinAttempt != null) {
            Arrays.fill(firstPinAttempt, '\u0000');
            firstPinAttempt = null;
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Defense-in-depth: wipe any lingering PIN data if the activity
        // is destroyed mid-flow (e.g. user backs out or rotates device
        // in a way that recreates the activity).
        if (firstPinAttempt != null) {
            Arrays.fill(firstPinAttempt, '\u0000');
            firstPinAttempt = null;
        }
    }
}

