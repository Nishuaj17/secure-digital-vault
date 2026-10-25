package com.example.securedigitalvault.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.prefs.AppPreferences;
import com.example.securedigitalvault.ui.dashboard.DashboardActivity;

/**
 * EnableBiometricActivity
 *
 * Offers the user the option to enable biometric unlock during
 * first-time setup. Uses BiometricManager to check device capability
 * before showing the option, and BiometricPrompt to confirm the
 * user's fingerprint/face actually works before saving the preference.
 *
 * SECURITY NOTE (see explanation above the code in chat): at this
 * stage we only record a boolean preference after a successful
 * biometric check. The deeper integration where biometric success
 * directly unlocks a Keystore-protected encryption key is added in
 * Phase 3, once KeystoreManager exists. We will revisit this class
 * at that point to wire biometric authentication to an actual
 * CryptoObject for real cryptographic protection.
 */
public class EnableBiometricActivity extends AppCompatActivity {

    private AppPreferences appPreferences;
    private BiometricManager biometricManager;

    private Button enableButton;
    private Button skipButton;
    private TextView unavailableText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable_biometric);

        appPreferences = new AppPreferences(getApplicationContext());
        biometricManager = BiometricManager.from(this);

        enableButton = findViewById(R.id.enableButton);
        skipButton = findViewById(R.id.skipButton);
        unavailableText = findViewById(R.id.unavailableText);

        checkBiometricAvailability();

        enableButton.setOnClickListener(v -> showBiometricPrompt());
        skipButton.setOnClickListener(v -> goToDashboard());
    }

    /**
     * Checks whether this device can actually use biometric auth right
     * now. A device might lack the hardware, or have hardware but no
     * fingerprints/faces enrolled in the OS settings yet.
     */
    private void checkBiometricAvailability() {
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
        );

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            // Hide the "enable" option entirely — there is nothing to enable.
            enableButton.setVisibility(View.GONE);
            unavailableText.setVisibility(View.VISIBLE);
        }
    }

    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirm biometric unlock")
                .setSubtitle("Verify it's you to enable this feature")
                .setNegativeButtonText("Cancel")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        appPreferences.setBiometricEnabled(true);
                        Toast.makeText(EnableBiometricActivity.this,
                                "Biometric unlock enabled", Toast.LENGTH_SHORT).show();
                        goToDashboard();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // User cancelled or an error occurred. We deliberately
                        // do NOT log errString in case it ever contains
                        // device-specific sensitive details — just show it
                        // to the user directly via Toast.
                        Toast.makeText(EnableBiometricActivity.this,
                                "Biometric setup cancelled: " + errString,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(EnableBiometricActivity.this,
                                "Fingerprint/face not recognized. Try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    private void goToDashboard() {
        Intent intent = new Intent(EnableBiometricActivity.this, DashboardActivity.class);
        // Clear the back stack so the user can't navigate back into the
        // setup flow using the system back button once setup is done.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}


