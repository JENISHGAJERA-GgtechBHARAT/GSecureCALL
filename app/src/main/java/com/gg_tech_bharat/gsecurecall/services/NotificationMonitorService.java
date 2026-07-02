package com.gg_tech_bharat.gsecurecall.services;

import android.app.KeyguardManager;
import android.app.Notification;
import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.gg_tech_bharat.gsecurecall.helpers.OverlayHelper;
import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;
import com.gg_tech_bharat.gsecurecall.utils.Constants;
import com.gg_tech_bharat.gsecurecall.utils.Logger;

public class NotificationMonitorService extends NotificationListenerService {

    private PreferenceHelper preferenceHelper;
    private KeyguardManager keyguardManager;

    @Override
    public void onCreate() {
        super.onCreate();
        preferenceHelper = PreferenceHelper.getInstance(this);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        Logger.d("NotificationMonitorService Created");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        // 1. Check if protection is enabled globally
        if (!preferenceHelper.isProtectionEnabled()) {
            return;
        }

        // 2. Check if the device is currently locked
        if (keyguardManager == null || !keyguardManager.isKeyguardLocked()) {
            // If the screen is unlocked/user is using the phone, never interfere.
            return;
        }

        String packageName = sbn.getPackageName();

        // 3. Check if the app posting the notification is in our protection list
        if (!preferenceHelper.isAppProtected(packageName)) {
            return;
        }

        // 4. Identify if notification is an incoming call
        Notification notification = sbn.getNotification();
        if (notification == null) return;

        boolean isCall = false;
        boolean isVideo = false;
        String callerName = "Incoming Call";

        // Method A: Check Category Call
        if (Notification.CATEGORY_CALL.equals(notification.category)) {
            isCall = true;
        }

        // Method B: Check Fullscreen Intent (Call notifications usually have this set)
        if (notification.fullScreenIntent != null) {
            isCall = true;
        }

        // Method C: Check action buttons (e.g., Answer, Decline, Accept, Reject)
        if (notification.actions != null) {
            for (Notification.Action action : notification.actions) {
                if (action.title != null) {
                    String title = action.title.toString().toLowerCase();
                    if (title.contains("answer") || title.contains("accept") || title.contains("decline") || title.contains("reject")) {
                        isCall = true;
                        break;
                    }
                }
            }
        }

        // Retrieve caller name
        if (notification.extras != null) {
            CharSequence titleCharSequence = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
            if (titleCharSequence != null) {
                callerName = titleCharSequence.toString();
            }
            
            // Check text for video call indications
            CharSequence textCharSequence = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
            if (textCharSequence != null) {
                String text = textCharSequence.toString().toLowerCase();
                if (text.contains("video")) {
                    isVideo = true;
                }
            }
        }

        // Additional Video Call keyword check in title
        if (callerName.toLowerCase().contains("video call")) {
            isVideo = true;
        }

        // 5. Apply User Preferences (Video Call vs. Voice Call locks)
        String requireLockFor = preferenceHelper.getRequireUnlockCallType(); // "voice", "video", "both"
        boolean shouldLock = false;

        if ("both".equals(requireLockFor)) {
            shouldLock = true;
        } else if ("voice".equals(requireLockFor) && !isVideo) {
            shouldLock = true;
        } else if ("video".equals(requireLockFor) && isVideo) {
            shouldLock = true;
        }

        boolean isOngoingActiveCall = false;
        if ((notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) {
            boolean hasAnswerAction = false;
            if (notification.actions != null) {
                for (Notification.Action action : notification.actions) {
                    if (action.title != null) {
                        String title = action.title.toString().toLowerCase();
                        if (title.contains("answer") || title.contains("accept") || title.contains("pick up") || title.contains("join")) {
                            hasAnswerAction = true;
                            break;
                        }
                    }
                }
            }
            if (!hasAnswerAction && isCall) {
                isOngoingActiveCall = true;
            }
        }

        if (isOngoingActiveCall && shouldLock) {
            Logger.i("Ongoing/answered call detected from: " + packageName + " - Showing overlay");
            preferenceHelper.setLastActivity("Blocked active call from " + callerName + " (" + packageName + ")");
            OverlayHelper.launchLockOverlay(this, packageName, callerName, isVideo);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        // Can be used to dismiss the overlay if the caller hangs up before authentication
        // For security & simplicity, overlay stays active or dismisses automatically when activity is finished.
    }
}
