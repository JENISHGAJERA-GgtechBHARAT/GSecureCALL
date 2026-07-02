package com.gg_tech_bharat.gsecurecall.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.gg_tech_bharat.gsecurecall.helpers.NotificationHelper;
import com.gg_tech_bharat.gsecurecall.utils.Constants;
import com.gg_tech_bharat.gsecurecall.utils.Logger;

public class ForegroundProtectionService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("ForegroundProtectionService Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("ForegroundProtectionService onStartCommand");
        
        Notification notification = NotificationHelper.getForegroundServiceNotification(this);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Target SDK 34+ requires service type declaration
            startForeground(Constants.FGS_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(Constants.FGS_NOTIFICATION_ID, notification);
        }

        return START_STICKY; // Keep service running
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("ForegroundProtectionService Destroyed");
    }
}
