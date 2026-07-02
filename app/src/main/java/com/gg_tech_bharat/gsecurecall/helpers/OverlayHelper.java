package com.gg_tech_bharat.gsecurecall.helpers;

import android.content.Context;
import android.content.Intent;

import com.gg_tech_bharat.gsecurecall.activities.LockOverlayActivity;
import com.gg_tech_bharat.gsecurecall.utils.Constants;
import com.gg_tech_bharat.gsecurecall.utils.Logger;

public class OverlayHelper {

    public static void launchLockOverlay(Context context, String packageName, String callerName, boolean isVideo) {
        Logger.d("Launching Lock Screen Overlay for: " + packageName + ", caller: " + callerName);
        
        Intent intent = new Intent(context, LockOverlayActivity.class);
        intent.putExtra(Constants.EXTRA_CALL_PACKAGE, packageName);
        intent.putExtra(Constants.EXTRA_CALLER_NAME, callerName);
        intent.putExtra(Constants.EXTRA_IS_VIDEO, isVideo);
        // Essential flags to launch Activity from a Service context
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                | Intent.FLAG_ACTIVITY_SINGLE_TOP 
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Logger.e("Failed to launch LockOverlayActivity", e);
        }
    }
}
