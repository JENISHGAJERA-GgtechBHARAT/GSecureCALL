package com.gg_tech_bharat.gsecurecall.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;
import com.gg_tech_bharat.gsecurecall.utils.Logger;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        PreferenceHelper prefs = PreferenceHelper.getInstance(context);
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            Logger.d("Screen ON detected");
            prefs.setLastActivity("Screen turned on");
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            Logger.d("Screen OFF detected");
            prefs.setLastActivity("Screen turned off");
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            Logger.d("User unlocked device");
            prefs.setLastActivity("Device unlocked by user");
        }
    }
}
