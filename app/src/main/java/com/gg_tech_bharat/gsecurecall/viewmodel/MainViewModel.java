package com.gg_tech_bharat.gsecurecall.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gg_tech_bharat.gsecurecall.helpers.PermissionHelper;
import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;

public class MainViewModel extends AndroidViewModel {

    private final PreferenceHelper preferenceHelper;
    private final MutableLiveData<Boolean> isProtectionEnabled = new MutableLiveData<>();
    private final MutableLiveData<Integer> protectedAppsCount = new MutableLiveData<>();
    private final MutableLiveData<Boolean> allPermissionsGranted = new MutableLiveData<>();
    private final MutableLiveData<String> lastActivity = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        preferenceHelper = PreferenceHelper.getInstance(application);
        refreshState();
    }

    public void refreshState() {
        boolean enabled = preferenceHelper.isProtectionEnabled();
        isProtectionEnabled.setValue(enabled);
        
        if (enabled) {
            protectedAppsCount.setValue(preferenceHelper.getProtectedApps().size());
        } else {
            protectedAppsCount.setValue(0);
        }
        
        lastActivity.setValue(preferenceHelper.getLastActivity());
        
        // Verify critical permissions: Accessibility and Notification Listener
        boolean permCheck = PermissionHelper.isAccessibilityServiceEnabled(getApplication())
                && PermissionHelper.isNotificationListenerEnabled(getApplication());
        allPermissionsGranted.setValue(permCheck);
    }

    public LiveData<Boolean> getIsProtectionEnabled() {
        return isProtectionEnabled;
    }

    public LiveData<Integer> getProtectedAppsCount() {
        return protectedAppsCount;
    }

    public LiveData<Boolean> getAllPermissionsGranted() {
        return allPermissionsGranted;
    }

    public LiveData<String> getLastActivity() {
        return lastActivity;
    }

    public void setProtectionEnabled(boolean enabled) {
        preferenceHelper.setProtectionEnabled(enabled);
        isProtectionEnabled.setValue(enabled);
        
        if (enabled) {
            protectedAppsCount.setValue(preferenceHelper.getProtectedApps().size());
        } else {
            protectedAppsCount.setValue(0);
        }
        
        String actionLog = enabled ? "Protection enabled manually" : "Protection disabled manually";
        preferenceHelper.setLastActivity(actionLog);
        lastActivity.setValue(actionLog);
    }
}
