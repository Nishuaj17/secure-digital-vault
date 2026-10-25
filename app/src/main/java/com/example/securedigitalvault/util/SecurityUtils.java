
package com.example.securedigitalvault.util;

import android.app.Activity;
import android.view.WindowManager;

public class SecurityUtils {

    public static void applySecureFlag(Activity activity) {
        if (activity == null || activity.getWindow() == null) {
            return;
        }

        activity.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SECURE
        );

        // Verify that the secure flag is enabled.
        boolean isSecure =
                (activity.getWindow().getAttributes().flags
                        & WindowManager.LayoutParams.FLAG_SECURE) != 0;

        android.util.Log.d(
                "SecurityUtils",
                "FLAG_SECURE enabled: " + isSecure
        );
    }
}
