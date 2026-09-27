package com.linknest.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ShortUrl {
    private String code;
    private String targetUrl;
    private String title;
    private long totalClicks = 0;
    private Map<String, Long> clicksByDevice = new HashMap<>(); // "Mobile", "Desktop", "Tablet", "Other"
    private Instant createdAt;

    public ShortUrl() {
        this.createdAt = Instant.now();
        this.clicksByDevice.put("Mobile", 0L);
        this.clicksByDevice.put("Desktop", 0L);
        this.clicksByDevice.put("Tablet", 0L);
        this.clicksByDevice.put("Other", 0L);
    }

    public ShortUrl(String code, String targetUrl, String title) {
        this();
        this.code = code;
        this.targetUrl = targetUrl;
        this.title = title != null && !title.isBlank() ? title : targetUrl;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(long totalClicks) {
        this.totalClicks = totalClicks;
    }

    public Map<String, Long> getClicksByDevice() {
        return clicksByDevice;
    }

    public void setClicksByDevice(Map<String, Long> clicksByDevice) {
        this.clicksByDevice = clicksByDevice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
