package com.gg_tech_bharat.gsecurecall.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;
import com.gg_tech_bharat.gsecurecall.services.ForegroundProtectionService;
import com.gg_tech_bharat.gsecurecall.utils.Logger;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Logger.d("BootReceiver: system boot completed");
            
            PreferenceHelper preferenceHelper = PreferenceHelper.getInstance(context);
            
            if (preferenceHelper.isProtectionEnabled() && preferenceHelper.isAutoStartBoot()) {
                Logger.i("BootReceiver: Auto-starting GSecureCALL Foreground service...");
                
                Intent serviceIntent = new Intent(context, ForegroundProtectionService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }
        }
    }
}
