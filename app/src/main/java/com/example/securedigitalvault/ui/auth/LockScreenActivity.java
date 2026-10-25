package com.example.securedigitalvault.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.prefs.AppPreferences;
import com.example.securedigitalvault.security.PinHasher;
import com.example.securedigitalvault.ui.dashboard.DashboardActivity;

import java.util.Arrays;

/**
 * LockScreenActivity
 *
 * Shown every time the app is opened by a returning user (i.e. setup
 * has already been completed). Verifies the PIN against the stored
 * PBKDF2 hash, and offers biometric unlock as an alternative if the
 * user enabled it during setup.
 *
 * SECURITY NOTES:
 * - PIN is compared using PinHasher.verifyPin(), which internally uses
 *   a constant-time comparison to avoid timing attacks.
 * - Failed attempts are tracked in memory only (not persisted) and
 *   trigger a short soft lockout after too many failures in a row.
 *   This is a basic deterrent, not a substitute for real device-level
 *   security — a determined attacker with local access could still
 *   reinstall the app or clear its data, which is a known limitation
 *   of a purely local, non-networked vault.
 * - As with EnableBiometricActivity, biometric success here currently
 *   just gates access at the UI level. Full cryptographic binding of
 *   biometric auth to the vault's encryption key is added in Phase 3.
 */
public class LockScreenActivity extends AppCompatActivity {

    private static final int PIN_LENGTH = 6;
    private static final int MAX_ATTEMPTS_BEFORE_LOCKOUT = 5;
    private static final long LOCKOUT_DURATION_MILLIS = 30_000; // 30 seconds

    private AppPreferences appPreferences;
    private BiometricManager biometricManager;

    private EditText pinInput;
    private TextView errorText;
    private Button unlockButton;
    private ImageButton biometricButton;

    private int failedAttempts = 0;
    private long lockoutUntilTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        appPreferences = new AppPreferences(getApplicationContext());
        biometricManager = BiometricManager.from(this);

        pinInput = findViewById(R.id.pinInput);
        errorText = findViewById(R.id.errorText);
        unlockButton = findViewById(R.id.unlockButton);
        biometricButton = findViewById(R.id.biometricButton);

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

        unlockButton.setOnClickListener(v -> attemptPinUnlock());

        boolean biometricEnabled = appPreferences.isBiometricEnabled();
        boolean biometricAvailable = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS;

        if (biometricEnabled && biometricAvailable) {
            biometricButton.setVisibility(View.VISIBLE);
            biometricButton.setOnClickListener(v -> showBiometricPrompt());
            // Automatically prompt on screen open for convenience.
            showBiometricPrompt();
        } else {
            biometricButton.setVisibility(View.GONE);
        }
    }

    private void attemptPinUnlock() {
        if (isCurrentlyLockedOut()) {
            showRemainingLockoutTime();
            return;
        }

        String typedText = pinInput.getText().toString();
        if (typedText.length() != PIN_LENGTH) {
            showError("Please enter your " + PIN_LENGTH + "-digit PIN.");
            return;
        }

        char[] typedPin = typedText.toCharArray();
        String storedHash = appPreferences.getPinHash();
        String storedSaltEncoded = appPreferences.getPinSalt();

        if (storedHash == null || storedSaltEncoded == null) {
            // This should never happen if setup was completed correctly,
            // but we handle it gracefully rather than crashing.
            showError("Vault security data is missing. Please reinstall the app.");
            Arrays.fill(typedPin, '\u0000');
            return;
        }

        byte[] salt = PinHasher.decodeSalt(storedSaltEncoded);
        boolean isCorrect = PinHasher.verifyPin(typedPin, storedHash, salt);
        Arrays.fill(typedPin, '\u0000');

        if (isCorrect) {
            failedAttempts = 0;
            goToDashboard();
        } else {
            failedAttempts++;
            pinInput.setText("");

            if (failedAttempts >= MAX_ATTEMPTS_BEFORE_LOCKOUT) {
                lockoutUntilTimestamp = System.currentTimeMillis() + LOCKOUT_DURATION_MILLIS;
                showRemainingLockoutTime();
            } else {
                int remaining = MAX_ATTEMPTS_BEFORE_LOCKOUT - failedAttempts;
                showError("Incorrect PIN. " + remaining + " attempt(s) remaining.");
            }
        }
    }

    private boolean isCurrentlyLockedOut() {
        return System.currentTimeMillis() < lockoutUntilTimestamp;
    }

    private void showRemainingLockoutTime() {
        long secondsLeft = (lockoutUntilTimestamp - System.currentTimeMillis()) / 1000;
        if (secondsLeft < 0) secondsLeft = 0;
        showError("Too many attempts. Try again in " + secondsLeft + "s.");
    }

    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Secure Vault")
                .setSubtitle("Use your fingerprint or face to continue")
                .setNegativeButtonText("Use PIN instead")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        failedAttempts = 0;
                        goToDashboard();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // User chose to fall back to PIN, or cancelled.
                        // No action needed — they can just use the PIN field.
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(LockScreenActivity.this,
                                "Fingerprint/face not recognized.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    private void goToDashboard() {
        Intent intent = new Intent(LockScreenActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}

