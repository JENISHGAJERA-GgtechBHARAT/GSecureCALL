package com.gg_tech_bharat.gsecurecall.services;

import android.accessibilityservice.AccessibilityService;
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

    @Override
    public void onCreate() {
        super.onCreate();
        preferenceHelper = PreferenceHelper.getInstance(this);
        keyguardManager = (Context.KEYGUARD_SERVICE != null) ? (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE) : null;
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
                    Logger.i("Accessibility: Answer button clicked. Triggering overlay lock.");
                    lastMinimizeTime = now;
                    preferenceHelper.setLastActivity("Locked answer action for " + packageName);
                    OverlayHelper.launchLockOverlay(this, packageName, "Incoming Call", false);
                }
                source.recycle();
            }
        }

        // Scenario B: Window state changed (e.g., they swiped to answer and the call screen became active)
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                if (isActiveCallState(rootNode)) {
                    Logger.i("Accessibility: Call is active/connected. Triggering overlay lock.");
                    lastMinimizeTime = now;
                    preferenceHelper.setLastActivity("Locked active call for " + packageName);
                    OverlayHelper.launchLockOverlay(this, packageName, "Incoming Call", false);
                }
                rootNode.recycle();
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

    private boolean isActiveCallState(AccessibilityNodeInfo node) {
        if (node == null) return false;
        
        boolean hasActiveButtons = hasTextInNodeTree(node, "mute") ||
                                   hasTextInNodeTree(node, "speaker") ||
                                   hasTextInNodeTree(node, "keypad") ||
                                   hasTextInNodeTree(node, "end call") ||
                                   hasTextInNodeTree(node, "hang up");
                                   
        boolean hasIncomingIndications = hasTextInNodeTree(node, "incoming") ||
                                         hasTextInNodeTree(node, "answer") ||
                                         hasTextInNodeTree(node, "accept") ||
                                         hasTextInNodeTree(node, "swipe");
                                         
        return hasActiveButtons && !hasIncomingIndications;
    }

    private boolean hasTextInNodeTree(AccessibilityNodeInfo node, String query) {
        if (node == null) return false;
        
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        if (text != null && text.toString().toLowerCase().contains(query)) {
            return true;
        }
        if (desc != null && desc.toString().toLowerCase().contains(query)) {
            return true;
        }
        
        // Check children (flat list check for battery optimization)
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                CharSequence cText = child.getText();
                CharSequence cDesc = child.getContentDescription();
                if (cText != null && cText.toString().toLowerCase().contains(query)) {
                    child.recycle();
                    return true;
                }
                if (cDesc != null && cDesc.toString().toLowerCase().contains(query)) {
                    child.recycle();
                    return true;
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
        Logger.d("Accessibility service connected");
    }
}
