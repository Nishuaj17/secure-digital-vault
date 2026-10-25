
package com.example.securedigitalvault;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.util.Log;

import com.example.securedigitalvault.data.prefs.AppPreferences;
import com.example.securedigitalvault.ui.auth.LockScreenActivity;
import com.example.securedigitalvault.ui.onboarding.EnableBiometricActivity;
import com.example.securedigitalvault.ui.onboarding.SetupPinActivity;
import com.example.securedigitalvault.ui.splash.SplashActivity;

public class SecureVaultApp extends Application
        implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "SecureVaultApp";

    private int startedActivityCount = 0;
    private long backgroundedAtTimestamp = 0;
    private Activity currentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    /**
     * Blocks ordinary screenshots and screen recording
     * for every Activity window in the application.
     */
    private void applyScreenshotProtection(Activity activity) {
        if (activity == null || activity.getWindow() == null) {
            return;
        }

        activity.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SECURE
        );

        boolean secureEnabled =
                (activity.getWindow().getAttributes().flags
                        & WindowManager.LayoutParams.FLAG_SECURE) != 0;

        Log.d(TAG, "FLAG_SECURE enabled for "
                + activity.getClass().getSimpleName()
                + ": " + secureEnabled);
    }

    @Override
    public void onActivityCreated(
            Activity activity, Bundle savedInstanceState) {
        applyScreenshotProtection(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        applyScreenshotProtection(activity);

        currentActivity = activity;
        startedActivityCount++;

        boolean isReturningFromFullBackground =
                startedActivityCount == 1
                        && backgroundedAtTimestamp != 0;

        if (isReturningFromFullBackground
                && shouldEnforceLock(activity)) {
            enforceLock(activity);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        applyScreenshotProtection(activity);
        currentActivity = activity;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        startedActivityCount--;

        if (startedActivityCount <= 0) {
            startedActivityCount = 0;
            backgroundedAtTimestamp = System.currentTimeMillis();
            currentActivity = null;
        }
    }

    private boolean shouldEnforceLock(Activity activity) {
        if (isAuthFlowScreen(activity)) {
            return false;
        }

        AppPreferences appPreferences =
                new AppPreferences(getApplicationContext());

        if (!appPreferences.isSetupComplete()) {
            return false;
        }

        int autoLockMinutes =
                appPreferences.getAutoLockMinutes();

        long elapsedMillis =
                System.currentTimeMillis() - backgroundedAtTimestamp;

        if (autoLockMinutes <= 0) {
            return true;
        }

        long thresholdMillis = autoLockMinutes * 60_000L;
        return elapsedMillis >= thresholdMillis;
    }

    private boolean isAuthFlowScreen(Activity activity) {
        return activity instanceof SplashActivity
                || activity instanceof SetupPinActivity
                || activity instanceof EnableBiometricActivity
                || activity instanceof LockScreenActivity;
    }

    private void enforceLock(Activity activity) {
        Intent intent = new Intent(
                activity, LockScreenActivity.class);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        activity.startActivity(intent);
    }

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(
            Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}
