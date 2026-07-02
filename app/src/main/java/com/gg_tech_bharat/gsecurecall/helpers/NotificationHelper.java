package com.gg_tech_bharat.gsecurecall.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.gg_tech_bharat.gsecurecall.activities.MainActivity;
import com.gg_tech_bharat.gsecurecall.utils.Constants;

public class NotificationHelper {

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "GSecureCALL Service";
            String description = "Keeps incoming call protection active in the background";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(Constants.FGS_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static Notification getForegroundServiceNotification(Context context) {
        createNotificationChannel(context);
        
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                notificationIntent, 
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(context, Constants.FGS_CHANNEL_ID)
                .setContentTitle("Call Protection Active")
                .setContentText("GSecureCALL is protecting your calls.")
                .setSmallIcon(android.R.drawable.ic_lock_lock) // Standard system lock icon for now, customized later
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    public static void showLockNotification(Context context, String packageName, String callerName, boolean isVideo) {
        String channelId = "gsecurecall_lock_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 1. Create High-Priority Channel for call trigger
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Secure Call Verification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setBypassDnd(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // 2. Intent to open LockOverlayActivity
        Intent intent = new Intent(context, com.gg_tech_bharat.gsecurecall.activities.LockOverlayActivity.class);
        intent.putExtra(Constants.EXTRA_CALL_PACKAGE, packageName);
        intent.putExtra(Constants.EXTRA_CALLER_NAME, callerName);
        intent.putExtra(Constants.EXTRA_IS_VIDEO, isVideo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                | Intent.FLAG_ACTIVITY_SINGLE_TOP 
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        // 3. Build high-priority notification with fullScreenIntent
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentTitle("Unlock Call")
                .setContentText("Tap to unlock and answer call")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(fullScreenPendingIntent, true) // Instantly open activity when screen is locked
                .setAutoCancel(true)
                .setOngoing(true);

        if (notificationManager != null) {
            notificationManager.notify(1002, builder.build());
        }
    }

    public static void cancelLockNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1002);
        }
    }
}
