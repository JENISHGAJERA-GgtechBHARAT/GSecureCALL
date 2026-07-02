package com.gg_tech_bharat.gsecurecall.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.gg_tech_bharat.gsecurecall.utils.Constants;

import java.util.HashSet;
import java.util.Set;

public class PreferenceHelper {
    private static PreferenceHelper instance;
    private final SharedPreferences prefs;

    private PreferenceHelper(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferenceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceHelper(context);
        }
        return instance;
    }

    // Protection Enabled
    public boolean isProtectionEnabled() {
        return prefs.getBoolean(Constants.KEY_PROTECTION_ENABLED, false);
    }

    public void setProtectionEnabled(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_PROTECTION_ENABLED, enabled).apply();
    }

    // Protected Applications List
    public Set<String> getProtectedApps() {
        return prefs.getStringSet(Constants.KEY_PROTECTED_APPS, new HashSet<>());
    }

    public void setProtectedApps(Set<String> appPackageNames) {
        prefs.edit().putStringSet(Constants.KEY_PROTECTED_APPS, appPackageNames).apply();
    }

    public boolean isAppProtected(String packageName) {
        Set<String> protectedApps = getProtectedApps();
        return protectedApps.contains(packageName);
    }

    public void addProtectedApp(String packageName) {
        Set<String> apps = new HashSet<>(getProtectedApps());
        apps.add(packageName);
        setProtectedApps(apps);
    }

    public void removeProtectedApp(String packageName) {
        Set<String> apps = new HashSet<>(getProtectedApps());
        apps.remove(packageName);
        setProtectedApps(apps);
    }

    // Theme Mode ("system", "light", "dark")
    public String getThemeMode() {
        return prefs.getString(Constants.KEY_THEME_MODE, "system");
    }

    public void setThemeMode(String theme) {
        prefs.edit().putString(Constants.KEY_THEME_MODE, theme).apply();
    }

    // Animation Speed ("normal", "fast", "slow")
    public String getAnimationSpeed() {
        return prefs.getString(Constants.KEY_ANIMATION_SPEED, "normal");
    }

    public void setAnimationSpeed(String speed) {
        prefs.edit().putString(Constants.KEY_ANIMATION_SPEED, speed).apply();
    }

    // Biometric Only
    public boolean isBiometricOnly() {
        return prefs.getBoolean(Constants.KEY_BIOMETRIC_ONLY, false);
    }

    public void setBiometricOnly(boolean biometricOnly) {
        prefs.edit().putBoolean(Constants.KEY_BIOMETRIC_ONLY, biometricOnly).apply();
    }

    // Allow Device Credentials
    public boolean isAllowDeviceCredential() {
        return prefs.getBoolean(Constants.KEY_ALLOW_DEVICE_CREDENTIAL, true);
    }

    public void setAllowDeviceCredential(boolean allow) {
        prefs.edit().putBoolean(Constants.KEY_ALLOW_DEVICE_CREDENTIAL, allow).apply();
    }

    // Require Unlock Call Type ("voice", "video", "both")
    public String getRequireUnlockCallType() {
        return prefs.getString(Constants.KEY_REQUIRE_UNLOCK_CALL_TYPE, "both");
    }

    public void setRequireUnlockCallType(String type) {
        prefs.edit().putString(Constants.KEY_REQUIRE_UNLOCK_CALL_TYPE, type).apply();
    }

    // Auto Start after Boot
    public boolean isAutoStartBoot() {
        return prefs.getBoolean(Constants.KEY_AUTO_START_BOOT, true);
    }

    public void setAutoStartBoot(boolean autoStart) {
        prefs.edit().putBoolean(Constants.KEY_AUTO_START_BOOT, autoStart).apply();
    }

    // Haptic Feedback
    public boolean isHapticFeedback() {
        return prefs.getBoolean(Constants.KEY_HAPTIC_FEEDBACK, true);
    }

    public void setHapticFeedback(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_HAPTIC_FEEDBACK, enabled).apply();
    }

    // Sound Effects
    public boolean isSoundEffects() {
        return prefs.getBoolean(Constants.KEY_SOUND_EFFECTS, true);
    }

    public void setSoundEffects(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_SOUND_EFFECTS, enabled).apply();
    }

    // Last Activity Logger
    public String getLastActivity() {
        return prefs.getString(Constants.KEY_LAST_ACTIVITY, "No recent activity");
    }

    public void setLastActivity(String log) {
        prefs.edit().putString(Constants.KEY_LAST_ACTIVITY, log).apply();
    }

    // Last Minimized Call Package
    public String getLastMinimizedPackage() {
        return prefs.getString("last_minimized_package", "");
    }

    public void setLastMinimizedPackage(String pkgName) {
        prefs.edit().putString("last_minimized_package", pkgName).apply();
    }
}
