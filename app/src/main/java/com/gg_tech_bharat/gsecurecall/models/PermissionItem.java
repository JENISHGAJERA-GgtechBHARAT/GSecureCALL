package com.gg_tech_bharat.gsecurecall.models;

public class PermissionItem {
    private final String permissionKey;
    private final String title;
    private final String description;
    private boolean isGranted;

    public PermissionItem(String permissionKey, String title, String description, boolean isGranted) {
        this.permissionKey = permissionKey;
        this.title = title;
        this.description = description;
        this.isGranted = isGranted;
    }

    public String getPermissionKey() {
        return permissionKey;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isGranted() {
        return isGranted;
    }

    public void setGranted(boolean granted) {
        isGranted = granted;
    }
}
