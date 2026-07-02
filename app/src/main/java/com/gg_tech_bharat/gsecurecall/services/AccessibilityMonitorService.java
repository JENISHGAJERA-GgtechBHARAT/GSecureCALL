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

        int eventType = event.getEventType();

        // Scenario A: User clicked the Answer/Accept button
        if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null) {
                if (isAnswerNode(source)) {
                    Logger.i("Accessibility: Answer button clicked. Triggering lock.");
                    triggerLock(packageName);
                }
                source.recycle();
            }
        }

        // Scenario B: Window content or state changed, check if call transitioned to active/ongoing
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                if (isActiveCallState(rootNode)) {
                    Logger.i("Accessibility: Call transitioned to Active/Ongoing state. Triggering lock.");
                    triggerLock(packageName);
                }
                rootNode.recycle();
            }
        }
    }

    private void triggerLock(String packageName) {
        // Traverse nodes to extract caller name or find calling indications
        String callerName = "Ongoing Call";
        boolean isVideo = false;
        
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            callerName = findCallerNameInNode(rootNode, callerName);
            isVideo = findIsVideoInNode(rootNode);
            rootNode.recycle();
        }

        // Apply User Preferences (Video Call vs. Voice Call locks)
        String requireLockFor = preferenceHelper.getRequireUnlockCallType(); // "voice", "video", "both"
        boolean shouldLock = false;

        if ("both".equals(requireLockFor)) {
            shouldLock = true;
        } else if ("voice".equals(requireLockFor) && !isVideo) {
            shouldLock = true;
        } else if ("video".equals(requireLockFor) && isVideo) {
            shouldLock = true;
        }

        if (shouldLock) {
            Logger.i("Accessibility triggering lock overlay.");
            preferenceHelper.setLastActivity("Locked pickup on " + packageName);
            OverlayHelper.launchLockOverlay(this, packageName, callerName, isVideo);
        }
    }

    private boolean isAnswerNode(AccessibilityNodeInfo node) {
        if (node == null) return false;
        
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        
        if (text != null && isAnswerText(text.toString())) {
            return true;
        }
        if (desc != null && isAnswerText(desc.toString())) {
            return true;
        }
        
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                boolean result = isAnswerNode(child);
                child.recycle();
                if (result) return true;
            }
        }
        return false;
    }
    
    private boolean isAnswerText(String str) {
        String s = str.toLowerCase();
        return s.contains("answer") || s.contains("accept") || s.contains("pick up") || 
               s.contains("join") || s.equals("call") || s.contains("swipe to answer");
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
                                         
        boolean hasDuration = hasDurationText(node);
        
        return (hasActiveButtons || hasDuration) && !hasIncomingIndications;
    }
    
    private boolean hasTextInNodeTree(AccessibilityNodeInfo node, String keyword) {
        if (node == null) return false;
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        if (text != null && text.toString().toLowerCase().contains(keyword)) {
            return true;
        }
        if (desc != null && desc.toString().toLowerCase().contains(keyword)) {
            return true;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                boolean result = hasTextInNodeTree(child, keyword);
                child.recycle();
                if (result) return true;
            }
        }
        return false;
    }
    
    private boolean hasDurationText(AccessibilityNodeInfo node) {
        if (node == null) return false;
        if (node.getText() != null) {
            String text = node.getText().toString();
            if (text.matches("\\d{2}:\\d{2}") || text.matches("\\d{2}:\\d{2}:\\d{2}")) {
                return true;
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                boolean result = hasDurationText(child);
                child.recycle();
                if (result) return true;
            }
        }
        return false;
    }

    private String findCallerNameInNode(AccessibilityNodeInfo node, String defaultName) {
        if (node == null) return defaultName;
        
        if (node.getText() != null) {
            String text = node.getText().toString();
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
    }
}
