package com.gg_tech_bharat.gsecurecall.models;

import android.graphics.drawable.Drawable;

public class ProtectedApplication {
    private final String packageName;
    private final String appName;
    private boolean isProtected;
    private transient Drawable appIcon; // Transient to exclude from serialization/DB

    public ProtectedApplication(String packageName, String appName, boolean isProtected) {
        this.packageName = packageName;
        this.appName = appName;
        this.isProtected = isProtected;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }
}
