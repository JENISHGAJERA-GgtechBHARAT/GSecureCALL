package com.gg_tech_bharat.gsecurecall.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;

public class SettingsViewModel extends AndroidViewModel {

    private final PreferenceHelper preferenceHelper;

    private final MutableLiveData<Boolean> protectionEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> themeMode = new MutableLiveData<>();
    private final MutableLiveData<String> animationSpeed = new MutableLiveData<>();
    private final MutableLiveData<Boolean> biometricOnly = new MutableLiveData<>();
    private final MutableLiveData<Boolean> allowDeviceCredential = new MutableLiveData<>();
    private final MutableLiveData<String> requireUnlockCallType = new MutableLiveData<>();
    private final MutableLiveData<Boolean> autoStartBoot = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hapticFeedback = new MutableLiveData<>();
    private final MutableLiveData<Boolean> soundEffects = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        preferenceHelper = PreferenceHelper.getInstance(application);
        loadSettings();
    }

    public void loadSettings() {
        protectionEnabled.setValue(preferenceHelper.isProtectionEnabled());
        themeMode.setValue(preferenceHelper.getThemeMode());
        animationSpeed.setValue(preferenceHelper.getAnimationSpeed());
        biometricOnly.setValue(preferenceHelper.isBiometricOnly());
        allowDeviceCredential.setValue(preferenceHelper.isAllowDeviceCredential());
        requireUnlockCallType.setValue(preferenceHelper.getRequireUnlockCallType());
        autoStartBoot.setValue(preferenceHelper.isAutoStartBoot());
        hapticFeedback.setValue(preferenceHelper.isHapticFeedback());
        soundEffects.setValue(preferenceHelper.isSoundEffects());
    }

    // Getters for LiveData
    public LiveData<Boolean> getProtectionEnabled() { return protectionEnabled; }
    public LiveData<String> getThemeMode() { return themeMode; }
    public LiveData<String> getAnimationSpeed() { return animationSpeed; }
    public LiveData<Boolean> getBiometricOnly() { return biometricOnly; }
    public LiveData<Boolean> getAllowDeviceCredential() { return allowDeviceCredential; }
    public LiveData<String> getRequireUnlockCallType() { return requireUnlockCallType; }
    public LiveData<Boolean> getAutoStartBoot() { return autoStartBoot; }
    public LiveData<Boolean> getHapticFeedback() { return hapticFeedback; }
    public LiveData<Boolean> getSoundEffects() { return soundEffects; }

    // Setters that persist and update LiveData
    public void setProtectionEnabled(boolean enabled) {
        preferenceHelper.setProtectionEnabled(enabled);
        protectionEnabled.setValue(enabled);
        preferenceHelper.setLastActivity(enabled ? "Protection enabled" : "Protection disabled");
    }

    public void setThemeMode(String theme) {
        preferenceHelper.setThemeMode(theme);
        themeMode.setValue(theme);
    }

    public void setAnimationSpeed(String speed) {
        preferenceHelper.setAnimationSpeed(speed);
        animationSpeed.setValue(speed);
    }

    public void setBiometricOnly(boolean enabled) {
        preferenceHelper.setBiometricOnly(enabled);
        biometricOnly.setValue(enabled);
    }

    public void setAllowDeviceCredential(boolean allowed) {
        preferenceHelper.setAllowDeviceCredential(allowed);
        allowDeviceCredential.setValue(allowed);
    }

    public void setRequireUnlockCallType(String type) {
        preferenceHelper.setRequireUnlockCallType(type);
        requireUnlockCallType.setValue(type);
    }

    public void setAutoStartBoot(boolean enabled) {
        preferenceHelper.setAutoStartBoot(enabled);
        autoStartBoot.setValue(enabled);
    }

    public void setHapticFeedback(boolean enabled) {
        preferenceHelper.setHapticFeedback(enabled);
        hapticFeedback.setValue(enabled);
    }

    public void setSoundEffects(boolean enabled) {
        preferenceHelper.setSoundEffects(enabled);
        soundEffects.setValue(enabled);
    }
}
