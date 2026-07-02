package com.gg_tech_bharat.gsecurecall.helpers;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;

import com.gg_tech_bharat.gsecurecall.services.AccessibilityMonitorService;
import com.gg_tech_bharat.gsecurecall.utils.Constants;

import java.util.List;

public class PermissionHelper {

    // 1. Accessibility Service check
    public static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) return false;
        
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo service : enabledServices) {
            ComponentName enabledServiceComponent = ComponentName.unflattenFromString(service.getId());
            if (enabledServiceComponent != null && 
                enabledServiceComponent.getPackageName().equals(context.getPackageName()) &&
                enabledServiceComponent.getClassName().equals(AccessibilityMonitorService.class.getName())) {
                return true;
            }
        }
        
        // Fallback check through Settings.Secure
        String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (settingValue != null) {
            String myService = context.getPackageName() + "/" + AccessibilityMonitorService.class.getName();
            return settingValue.contains(myService);
        }
        
        return false;
    }

    public static void openAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // 2. Notification Listener Service check
    public static boolean isNotificationListenerEnabled(Context context) {
        String enabledListeners = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        String myListener = context.getPackageName();
        return enabledListeners != null && enabledListeners.contains(myListener);
    }

    public static void openNotificationListenerSettings(Context context) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        } else {
            intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // 3. Display Over Other Apps (Overlay) check
    public static boolean isOverlayPermissionEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    public static void openOverlaySettings(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    // 4. Foreground Service permission check
    public static boolean isForegroundPermissionEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Auto-granted on older APIs
    }

    // 5. Battery Optimization status check
    public static boolean isBatteryOptimizationIgnored(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                return pm.isIgnoringBatteryOptimizations(context.getPackageName());
            }
        }
        return true;
    }

    public static void requestIgnoreBatteryOptimizations(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    // 6. Biometric lock enrollment check
    public static boolean isBiometricEnrolled(Context context) {
        BiometricManager bm = BiometricManager.from(context);
        int authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG;
        if (PreferenceHelper.getInstance(context).isAllowDeviceCredential()) {
            authenticators |= BiometricManager.Authenticators.DEVICE_CREDENTIAL;
        }
        return bm.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static boolean isPermissionGranted(Context context, String key) {
        switch (key) {
            case Constants.PERM_ACCESSIBILITY:
                return isAccessibilityServiceEnabled(context);
            case Constants.PERM_NOTIFICATION:
                return isNotificationListenerEnabled(context);
            case Constants.PERM_OVERLAY:
                return isOverlayPermissionEnabled(context);
            case Constants.PERM_FOREGROUND:
                return isForegroundPermissionEnabled(context);
            case Constants.PERM_BATTERY:
                return isBatteryOptimizationIgnored(context);
            case Constants.PERM_BIOMETRIC:
                return isBiometricEnrolled(context);
            default:
                return false;
        }
    }
}
