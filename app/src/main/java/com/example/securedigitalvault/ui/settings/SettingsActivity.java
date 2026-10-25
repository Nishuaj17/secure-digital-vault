package com.example.securedigitalvault.ui.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.prefs.AppPreferences;
import com.example.securedigitalvault.ui.auth.LockScreenActivity;
import com.example.securedigitalvault.util.SecurityUtils;

public class SettingsActivity extends AppCompatActivity {

    private AppPreferences appPreferences;
    private BiometricManager biometricManager;

    private Switch biometricSwitch;
    private TextView autoLockValueText;

    // Auto-lock options shown to the user, mapped to minutes stored in
    // AppPreferences. "Immediately" is represented as 0 minutes.
    private final String[] autoLockLabels = {"Immediately", "1 minute", "5 minutes", "15 minutes"};
    private final int[] autoLockMinuteValues = {0, 1, 5, 15};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurityUtils.applySecureFlag(this);
        setContentView(R.layout.activity_settings);

        appPreferences = new AppPreferences(getApplicationContext());
        biometricManager = BiometricManager.from(this);

        biometricSwitch = findViewById(R.id.biometricSwitch);
        autoLockValueText = findViewById(R.id.autoLockValueText);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.changePinRow).setOnClickListener(v ->
                startActivity(new Intent(this, ChangePinActivity.class)));
        findViewById(R.id.autoLockRow).setOnClickListener(v -> showAutoLockPicker());
        findViewById(R.id.lockNowRow).setOnClickListener(v -> lockVaultNow());
        findViewById(R.id.manageCategoriesRow).setOnClickListener(v ->
                startActivity(new Intent(this, CategoryManagerActivity.class)));

        setupBiometricSwitch();
        refreshAutoLockLabel();
    }

    private void setupBiometricSwitch() {
        biometricSwitch.setChecked(appPreferences.isBiometricEnabled());

        biometricSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                boolean available = biometricManager.canAuthenticate(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS;
                if (!available) {
                    Toast.makeText(this, "Biometric authentication isn't available on this device.", Toast.LENGTH_SHORT).show();
                    biometricSwitch.setChecked(false);
                    return;
                }
            }
            appPreferences.setBiometricEnabled(isChecked);
        });
    }

    private void showAutoLockPicker() {
        int currentMinutes = appPreferences.getAutoLockMinutes();
        int currentIndex = 1; // default to "1 minute" if no exact match found
        for (int i = 0; i < autoLockMinuteValues.length; i++) {
            if (autoLockMinuteValues[i] == currentMinutes) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Auto-lock after")
                .setSingleChoiceItems(autoLockLabels, currentIndex, (dialog, which) -> {
                    appPreferences.setAutoLockMinutes(autoLockMinuteValues[which]);
                    refreshAutoLockLabel();
                    dialog.dismiss();
                })
                .show();
    }

    private void refreshAutoLockLabel() {
        int currentMinutes = appPreferences.getAutoLockMinutes();
        for (int i = 0; i < autoLockMinuteValues.length; i++) {
            if (autoLockMinuteValues[i] == currentMinutes) {
                autoLockValueText.setText(autoLockLabels[i]);
                return;
            }
        }
        autoLockValueText.setText(currentMinutes + " minute(s)");
    }

    private void lockVaultNow() {
        Intent intent = new Intent(this, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}


