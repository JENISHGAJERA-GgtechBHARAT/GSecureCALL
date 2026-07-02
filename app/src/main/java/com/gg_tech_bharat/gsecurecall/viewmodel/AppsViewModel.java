package com.gg_tech_bharat.gsecurecall.viewmodel;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;
import com.gg_tech_bharat.gsecurecall.models.ProtectedApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppsViewModel extends AndroidViewModel {

    private final PreferenceHelper preferenceHelper;
    private final MutableLiveData<List<ProtectedApplication>> appsList = new MutableLiveData<>();
    private final MutableLiveData<List<ProtectedApplication>> remainingAppsList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public AppsViewModel(@NonNull Application application) {
        super(application);
        preferenceHelper = PreferenceHelper.getInstance(application);
        loadInstalledApps();
    }

    public LiveData<List<ProtectedApplication>> getAppsList() {
        return appsList;
    }

    public LiveData<List<ProtectedApplication>> getRemainingAppsList() {
        return remainingAppsList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadInstalledApps() {
        isLoading.setValue(true);
        executorService.execute(() -> {
            PackageManager pm = getApplication().getPackageManager();
            List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            List<ProtectedApplication> callApps = new ArrayList<>();
            List<ProtectedApplication> remainingApps = new ArrayList<>();
            Set<String> protectedApps = preferenceHelper.getProtectedApps();
            String myPkg = getApplication().getPackageName();

            for (ApplicationInfo info : installedApps) {
                if (pm.getLaunchIntentForPackage(info.packageName) != null && !info.packageName.equals(myPkg)) {
                    String label = info.loadLabel(pm).toString();
                    boolean isProtected = protectedApps.contains(info.packageName);
                    
                    ProtectedApplication appItem = new ProtectedApplication(info.packageName, label, isProtected);
                    
                    try {
                        Drawable icon = info.loadIcon(pm);
                        appItem.setAppIcon(icon);
                    } catch (Exception e) {
                        // Handled in adapter
                    }
                    
                    // If app has call permissions/features OR is already manually protected, show in main list
                    if (isCallApp(pm, info) || isProtected) {
                        callApps.add(appItem);
                    } else {
                        remainingApps.add(appItem);
                    }
                }
            }

            // Sort lists alphabetically
            Collections.sort(callApps, (a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName()));
            Collections.sort(remainingApps, (a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName()));

            appsList.postValue(callApps);
            remainingAppsList.postValue(remainingApps);
            isLoading.postValue(false);
        });
    }

    private boolean isCallApp(PackageManager pm, ApplicationInfo info) {
        // 1. Check App Category (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (info.category == ApplicationInfo.CATEGORY_SOCIAL) {
                return true;
            }
        }

        // 2. Check Package Name heuristics
        String pkg = info.packageName.toLowerCase();
        if (pkg.contains("dialer") || pkg.contains("phone") || pkg.contains("whatsapp") ||
            pkg.contains("telegram") || pkg.contains("signal") || pkg.contains("discord") ||
            pkg.contains("teams") || pkg.contains("zoom") || pkg.contains("skype") ||
            pkg.contains("messenger") || pkg.contains("instagram") || pkg.contains("snapchat") ||
            pkg.contains("duo") || pkg.contains("meet") || pkg.contains("viber") ||
            pkg.contains("call") || pkg.contains("chat") || pkg.contains("social")) {
            return true;
        }

        // 3. Check requested permissions in Manifest
        try {
            android.content.pm.PackageInfo packageInfo = pm.getPackageInfo(info.packageName, PackageManager.GET_PERMISSIONS);
            if (packageInfo.requestedPermissions != null) {
                for (String perm : packageInfo.requestedPermissions) {
                    if (perm.equals(android.Manifest.permission.RECORD_AUDIO) ||
                        perm.equals(android.Manifest.permission.CAMERA) ||
                        perm.equals(android.Manifest.permission.CALL_PHONE) ||
                        perm.equals(android.Manifest.permission.MANAGE_OWN_CALLS) ||
                        perm.equals(android.Manifest.permission.USE_SIP)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore if package permissions cannot be retrieved
        }

        return false;
    }

    public void toggleAppProtection(ProtectedApplication app, boolean isProtected) {
        if (isProtected) {
            preferenceHelper.addProtectedApp(app.getPackageName());
        } else {
            preferenceHelper.removeProtectedApp(app.getPackageName());
        }
        
        String lastAct = (isProtected ? "Protected " : "Unprotected ") + app.getAppName();
        preferenceHelper.setLastActivity(lastAct);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
