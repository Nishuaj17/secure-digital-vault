package com.example.securedigitalvault.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * ClipboardHelper
 *
 * Copies sensitive text (passwords, card numbers, recovery codes) to
 * the system clipboard and automatically clears it after a short
 * delay.
 *
 * ANDROID CLIPBOARD PRIVACY LIMITATIONS (worth understanding):
 * - On Android 12 and earlier, any app on the device could read
 *   clipboard contents at any time, even in the background. Auto-clear
 *   reduces the exposure window but cannot prevent a malicious app
 *   from reading the clipboard in the few seconds before it clears.
 * - On Android 13+, the OS shows a visual confirmation toast whenever
 *   something is copied, and most apps can no longer silently read
 *   clipboard content in the background without the user's own
 *   clipboard access being active — this is a platform-level privacy
 *   improvement we benefit from automatically, not something this app
 *   implements itself.
 * - We cannot guarantee another app hasn't already read the clipboard
 *   in the moment between copy and auto-clear. This is a fundamental
 *   limitation of using the system clipboard at all, not a bug in this
 *   implementation.
 */
public class ClipboardHelper {

    private static final long AUTO_CLEAR_DELAY_MILLIS = 30_000; // 30 seconds

    public static void copyWithAutoClear(Context context, String label, String sensitiveText) {
        ClipboardManager clipboardManager =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboardManager == null) {
            return;
        }

        ClipData clip = ClipData.newPlainText(label, sensitiveText);
        clipboardManager.setPrimaryClip(clip);

        Toast.makeText(context, label + " copied. Clipboard clears in 30s.", Toast.LENGTH_SHORT).show();

        // Schedule clipboard clearing. We overwrite it with an empty
        // clip rather than assuming any "clear" API, since Android
        // does not provide a direct clipboard-clear method — replacing
        // the content is the standard approach.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ClipData emptyClip = ClipData.newPlainText("", "");
            clipboardManager.setPrimaryClip(emptyClip);
        }, AUTO_CLEAR_DELAY_MILLIS);
    }
}

