package com.gg_tech_bharat.gsecurecall.services;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;
import com.gg_tech_bharat.gsecurecall.utils.Logger;

import java.util.List;

public class AccessibilityMonitorService extends AccessibilityService {

    private PreferenceHelper preferenceHelper;
    private KeyguardManager keyguardManager;
    private static long lastMinimizeTime = 0;
    private static final long MINIMIZE_COOLDOWN_MS = 8000; // 8 seconds cooldown

    private final BroadcastReceiver unlockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                String pkg = preferenceHelper.getLastMinimizedPackage();
                if (pkg != null && !pkg.isEmpty()) {
                    Logger.d("User unlocked device. Restoring call screen task for: " + pkg);
                    
                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    boolean movedTask = false;
                    
                    if (am != null) {
                        try {
                            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(10);
                            for (ActivityManager.RunningTaskInfo taskInfo : tasks) {
                                if (taskInfo.baseActivity != null && taskInfo.baseActivity.getPackageName().equals(pkg)) {
                                    am.moveTaskToFront(taskInfo.id, 0);
                                    movedTask = true;
                                    Logger.d("Moved call task to front successfully: " + taskInfo.id);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            Logger.e("Failed to move task to front using ActivityManager", e);
                        }
                    }

                    // Fallback to launch intent if task reordering failed
                    if (!movedTask) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pkg);
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT 
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            try {
                                startActivity(launchIntent);
                                Logger.d("Launched package fallback: " + pkg);
                            } catch (Exception e) {
                                Logger.e("Failed to restore call screen on unlock fallback", e);
                            }
                        }
                    }
                    
                    preferenceHelper.setLastMinimizedPackage(""); // Clear after restoring
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        preferenceHelper = PreferenceHelper.getInstance(this);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        
        // Register BroadcastReceiver for user present (unlock)
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        registerReceiver(unlockReceiver, filter);
        
        Logger.d("AccessibilityMonitorService Created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(unlockReceiver);
        } catch (Exception e) {
            Logger.w("Failed to unregister receiver: " + e.getMessage());
        }
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
                    Logger.i("Accessibility: Answer button clicked. Starting direct answer and minimize delay.");
                    lastMinimizeTime = now;
                    preferenceHelper.setLastMinimizedPackage(packageName);
                    preferenceHelper.setLastActivity("Minimized answer action for " + packageName);
                    
                    // Allow 100ms delay for the app to register click and transition call state
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        Logger.i("Accessibility: Minimizing call to show default lock screen.");
                        performGlobalAction(GLOBAL_ACTION_HOME);
                    }, 100);
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
        Logger.d("Accessibility service connected");
    }
}
