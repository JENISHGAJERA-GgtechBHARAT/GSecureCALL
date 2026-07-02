package com.gg_tech_bharat.gsecurecall.services;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.gg_tech_bharat.gsecurecall.helpers.OverlayHelper;
import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;
import com.gg_tech_bharat.gsecurecall.utils.Logger;

import java.util.List;

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

        // 5. Look for window state changes (which indicate a call screen appeared)
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Logger.d("Window state changed for protected package: " + packageName);
            
            // Traverse nodes to extract caller name or find calling indications
            String callerName = "Incoming Call";
            boolean isVideo = false;
            
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                // Look for text nodes that might represent the caller or call type
                callerName = findCallerNameInNode(rootNode, callerName);
                isVideo = findIsVideoInNode(rootNode);
                rootNode.recycle();
            }

            Logger.i("Accessibility detected call window. Launching overlay.");
            preferenceHelper.setLastActivity("Blocked call window on " + packageName);
            OverlayHelper.launchLockOverlay(this, packageName, callerName, isVideo);
        }
    }

    private String findCallerNameInNode(AccessibilityNodeInfo node, String defaultName) {
        if (node == null) return defaultName;
        
        // WhatsApp call windows usually contain nodes with name or duration.
        // We look for text nodes that are not status/action buttons
        if (node.getText() != null) {
            String text = node.getText().toString();
            // Basic heuristics to filter out standard labels
            if (!text.isEmpty() && text.length() < 30 && 
                !text.toLowerCase().contains("call") && 
                !text.toLowerCase().contains("answer") && 
                !text.toLowerCase().contains("decline") && 
                !text.toLowerCase().contains("swipe") &&
                !text.toLowerCase().contains("mute")) {
                return text;
            }
        }
        
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                String found = findCallerNameInNode(child, null);
                child.recycle();
                if (found != null) {
                    return found;
                }
            }
        }
        return defaultName;
    }

    private boolean findIsVideoInNode(AccessibilityNodeInfo node) {
        if (node == null) return false;
        if (node.getText() != null) {
            String text = node.getText().toString().toLowerCase();
            if (text.contains("video call") || text.contains("incoming video")) {
                return true;
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                boolean result = findIsVideoInNode(child);
                child.recycle();
                if (result) return true;
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
        Logger.d("Accessibility service connected");
        // Configuration is done in accessibility_service_config.xml
    }
}
