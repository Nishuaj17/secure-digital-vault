package com.example.securedigitalvault.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.prefs.AppPreferences;
import com.example.securedigitalvault.ui.auth.LockScreenActivity;
import com.example.securedigitalvault.ui.onboarding.SetupPinActivity;

/**
 * SplashActivity
 *
 * The very first screen shown when the app launches. Displays the
 * brand for a short moment, then routes the user to either:
 *   - SetupPinActivity, if this is the first time the app has been opened
 *   - LockScreenActivity, if a PIN has already been configured
 *
 * SECURITY NOTE: This activity does not display or handle any sensitive
 * data. It only reads a single boolean flag (isSetupComplete) to decide
 * where to navigate.
 */
public class SplashActivity extends AppCompatActivity {

    // A short, deliberate delay purely for branding purposes.
    // This is NOT a security delay of any kind.
    private static final long SPLASH_DELAY_MILLIS = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppPreferences appPreferences = new AppPreferences(getApplicationContext());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent nextScreenIntent;

            if (appPreferences.isSetupComplete()) {
                nextScreenIntent = new Intent(SplashActivity.this, LockScreenActivity.class);
            } else {
                nextScreenIntent = new Intent(SplashActivity.this, SetupPinActivity.class);
            }

            startActivity(nextScreenIntent);
            // finish() removes SplashActivity from the back stack so the
            // user can never press "back" and return to the splash screen.
            finish();

        }, SPLASH_DELAY_MILLIS);
    }
}


