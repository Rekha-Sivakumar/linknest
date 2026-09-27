package com.linknest.model;

import java.time.Instant;

public class ClickEvent {
    private String id;
    private Instant timestamp;
    private String targetType; // "BIO_LINK" or "SHORT_URL" or "PROFILE_VIEW"
    private String targetId;   // linkId or shortCode or username
    private String targetTitle;
    private String deviceType; // "Mobile", "Desktop", "Tablet", "Other"
    private String referrer;

    public ClickEvent() {
        this.timestamp = Instant.now();
    }

    public ClickEvent(String id, String targetType, String targetId, String targetTitle, String deviceType, String referrer) {
        this();
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetTitle = targetTitle;
        this.deviceType = deviceType;
        this.referrer = referrer != null && !referrer.isBlank() ? referrer : "Direct";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetTitle() {
        return targetTitle;
    }

    public void setTargetTitle(String targetTitle) {
        this.targetTitle = targetTitle;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
}
