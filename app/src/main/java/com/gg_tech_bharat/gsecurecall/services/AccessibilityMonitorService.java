package com.gg_tech_bharat.gsecurecall.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.gg_tech_bharat.gsecurecall.helpers.OverlayHelper;
import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;
import com.gg_tech_bharat.gsecurecall.utils.Logger;

public class AccessibilityMonitorService extends AccessibilityService {

    private PreferenceHelper preferenceHelper;
    private KeyguardManager keyguardManager;
    private static long lastMinimizeTime = 0;
    private static final long MINIMIZE_COOLDOWN_MS = 8000; // 8 seconds cooldown

    private static AccessibilityMonitorService instance;
    public static AccessibilityMonitorService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        preferenceHelper = PreferenceHelper.getInstance(this);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        Logger.d("AccessibilityMonitorService Created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Logger.d("AccessibilityMonitorService Destroyed");
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

        int eventType = event.getEventType();

        // Apply cooldown to prevent duplicate triggers
        long now = System.currentTimeMillis();
        if (now - lastMinimizeTime < MINIMIZE_COOLDOWN_MS) {
            return;
        }

        // Scenario A: User clicked the Answer/Accept button
        if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null) {
                if (isAnswerNode(source)) {
                    Logger.i("Accessibility: Answer button clicked. Triggering keyguard lock overlay.");
                    lastMinimizeTime = now;
                    preferenceHelper.setLastActivity("Locked answer action for " + packageName);
                    OverlayHelper.launchLockOverlay(this, packageName, "Incoming Call", false);
                }
                source.recycle();
            }
        }
    }

    private boolean isAnswerNode(AccessibilityNodeInfo node) {
        if (node == null) return false;
        
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        
        if (text != null) {
            String str = text.toString().toLowerCase();
            if (str.contains("answer") || str.contains("accept") || str.contains("pick up") || str.contains("join")) {
                return true;
            }
        }
        if (desc != null) {
            String str = desc.toString().toLowerCase();
            if (str.contains("answer") || str.contains("accept") || str.contains("pick up") || str.contains("join")) {
                return true;
            }
        }
        
        // Check children (flat, 1 level deep for speed/battery saving)
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                CharSequence cText = child.getText();
                CharSequence cDesc = child.getContentDescription();
                if (cText != null) {
                    String str = cText.toString().toLowerCase();
                    if (str.contains("answer") || str.contains("accept") || str.contains("pick up") || str.contains("join")) {
                        child.recycle();
                        return true;
                    }
                }
                if (cDesc != null) {
                    String str = cDesc.toString().toLowerCase();
                    if (str.contains("answer") || str.contains("accept") || str.contains("pick up") || str.contains("join")) {
                        child.recycle();
                        return true;
                    }
                }
                child.recycle();
            }
        }
        
        return false;
    }

    @Override
    public void onInterrupt() {
        Logger.w("Accessibility service interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        updateAccessibilityServiceInfo();
        Logger.d("Accessibility service connected");
    }

    public void updateAccessibilityServiceInfo() {
        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) {
            info = new AccessibilityServiceInfo();
        }
        
        java.util.Set<String> protectedApps = preferenceHelper.getProtectedApps();
        if (protectedApps != null && !protectedApps.isEmpty()) {
            info.packageNames = protectedApps.toArray(new String[0]);
        } else {
            // Monitor a dummy package so we don't scan everything (including Paytm)
            info.packageNames = new String[]{"com.gg_tech_bharat.gsecurecall.dummy"};
        }
        
        setServiceInfo(info);
        Logger.d("Updated AccessibilityService package filter to: " + java.util.Arrays.toString(info.packageNames));
    }
}
