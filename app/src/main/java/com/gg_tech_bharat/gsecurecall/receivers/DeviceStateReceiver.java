package com.gg_tech_bharat.gsecurecall.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;
import com.gg_tech_bharat.gsecurecall.utils.Logger;

public class DeviceStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        PreferenceHelper prefs = PreferenceHelper.getInstance(context);
        if (Intent.ACTION_BATTERY_LOW.equals(action)) {
            Logger.w("Device battery is running low");
            prefs.setLastActivity("Battery low alert");
        } else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
            Logger.d("Power charger connected");
            prefs.setLastActivity("Charger connected");
        } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
            Logger.d("Power charger disconnected");
            prefs.setLastActivity("Charger disconnected");
        }
    }
}
