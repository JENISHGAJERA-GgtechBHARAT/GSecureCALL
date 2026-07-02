package com.gg_tech_bharat.gsecurecall.models;

public class CallInfo {
    private final String packageName;
    private final String callerName;
    private final boolean isVideo;
    private final long timestamp;

    public CallInfo(String packageName, String callerName, boolean isVideo, long timestamp) {
        this.packageName = packageName;
        this.callerName = callerName;
        this.isVideo = isVideo;
        this.timestamp = timestamp;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getCallerName() {
        return callerName;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
