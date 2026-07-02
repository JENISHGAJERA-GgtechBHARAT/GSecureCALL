package com.gg_tech_bharat.gsecurecall.utils;

public class Constants {
    public static final String PREFS_NAME = "GSecureCALLPrefs";
    
    // Preference Keys
    public static final String KEY_PROTECTION_ENABLED = "protection_enabled";
    public static final String KEY_PROTECTED_APPS = "protected_apps"; // Set of package names
    public static final String KEY_THEME_MODE = "theme_mode"; // "system", "light", "dark"
    public static final String KEY_ANIMATION_SPEED = "animation_speed"; // "normal", "fast", "slow"
    public static final String KEY_BIOMETRIC_ONLY = "biometric_only";
    public static final String KEY_ALLOW_DEVICE_CREDENTIAL = "allow_device_credential";
    public static final String KEY_REQUIRE_UNLOCK_CALL_TYPE = "require_unlock_call_type"; // "voice", "video", "both"
    public static final String KEY_AUTO_START_BOOT = "auto_start_boot";
    public static final String KEY_HAPTIC_FEEDBACK = "haptic_feedback";
    public static final String KEY_SOUND_EFFECTS = "sound_effects";
    public static final String KEY_LAST_ACTIVITY = "last_activity";

    // Intent Actions & Extras
    public static final String ACTION_TRIGGER_OVERLAY = "com.gg_tech_bharat.gsecurecall.TRIGGER_OVERLAY";
    public static final String EXTRA_CALL_PACKAGE = "extra_call_package";
    public static final String EXTRA_CALLER_NAME = "extra_caller_name";
    public static final String EXTRA_IS_VIDEO = "extra_is_video";

    // Service Constants
    public static final String FGS_CHANNEL_ID = "GSecureCALL_FGS_Channel";
    public static final int FGS_NOTIFICATION_ID = 2001;

    // Permission Keys for checking status
    public static final String PERM_ACCESSIBILITY = "perm_accessibility";
    public static final String PERM_NOTIFICATION = "perm_notification";
    public static final String PERM_OVERLAY = "perm_overlay";
    public static final String PERM_FOREGROUND = "perm_foreground";
    public static final String PERM_BATTERY = "perm_battery";
    public static final String PERM_BIOMETRIC = "perm_biometric";
}
