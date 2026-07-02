package com.gg_tech_bharat.gsecurecall.services;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;

import com.gg_tech_bharat.gsecurecall.helpers.OverlayHelper;
import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;
import com.gg_tech_bharat.gsecurecall.utils.Logger;

public class AccessibilityMonitorService extends AccessibilityService {

    private PreferenceHelper preferenceHelper;
    private KeyguardManager keyguardManager;

    @Override
    public void onCreate() {
        super.onCreate();
        preferenceHelper = PreferenceHelper.getInstance(this);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        Logger.d("AccessibilityMonitorService Created");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 1. Check if protection is enabled globally
        if (!preferenceHelper.isProtectionEnabled()) {
            return;
        }

        // 2. Check if the screen is locked
        if (keyguardManager == null || !keyguardManager.isKeyguardLocked()) {
            return;
        }

        // 3. Inspect package name of the event
        if (event.getPackageName() == null) return;
        String packageName = event.getPackageName().toString();

        // 4. Check if this is a protected application
        if (!preferenceHelper.isAppProtected(packageName)) {
            return;
        }

        // 5. Trigger lock on window state change (instant 0ms trigger, no recursive layout traversal)
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Logger.i("Accessibility: Protected call window opened. Triggering lock.");
            preferenceHelper.setLastActivity("Locked call window on " + packageName);
            
            // Check call type preferences
            String requireLockFor = preferenceHelper.getRequireUnlockCallType(); // "voice", "video", "both"
            boolean shouldLock = "both".equals(requireLockFor) || "voice".equals(requireLockFor) || "video".equals(requireLockFor);
            
            if (shouldLock) {
                OverlayHelper.launchLockOverlay(this, packageName, "Incoming Call", false);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Logger.w("Accessibility service interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Logger.d("Accessibility service connected");
    }
}
