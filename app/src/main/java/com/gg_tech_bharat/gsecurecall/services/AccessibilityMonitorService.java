package com.gg_tech_bharat.gsecurecall.services;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;

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

        // 5. Minimize call window to force default system lock screen
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Logger.i("Accessibility: Protected call window opened while locked. Minimizing call screen.");
            preferenceHelper.setLastActivity("Minimized call window for " + packageName + " to force default lock screen");
            
            // Check call type preferences
            String requireLockFor = preferenceHelper.getRequireUnlockCallType(); // "voice", "video", "both"
            boolean shouldLock = "both".equals(requireLockFor) || "voice".equals(requireLockFor) || "video".equals(requireLockFor);
            
            if (shouldLock) {
                // Perform global action to press the Home button
                // This minimizes the call screen to the background and forces the default system keyguard to show
                performGlobalAction(GLOBAL_ACTION_HOME);
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
