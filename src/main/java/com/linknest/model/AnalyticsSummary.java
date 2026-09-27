package com.linknest.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsSummary {
    private long totalPageViews;
    private long totalBioLinkClicks;
    private long totalShortUrlClicks;
    private long grandTotalInteractions;
    private Map<String, Long> deviceDistribution = new HashMap<>();
    private List<LinkItem> topLinks = new ArrayList<>();
    private List<ShortUrl> topShortUrls = new ArrayList<>();
    private List<ClickEvent> recentActivity = new ArrayList<>();

    public AnalyticsSummary() {}

    public long getTotalPageViews() {
        return totalPageViews;
    }

    public void setTotalPageViews(long totalPageViews) {
        this.totalPageViews = totalPageViews;
    }

    public long getTotalBioLinkClicks() {
        return totalBioLinkClicks;
    }

    public void setTotalBioLinkClicks(long totalBioLinkClicks) {
        this.totalBioLinkClicks = totalBioLinkClicks;
    }

    public long getTotalShortUrlClicks() {
        return totalShortUrlClicks;
    }

    public void setTotalShortUrlClicks(long totalShortUrlClicks) {
        this.totalShortUrlClicks = totalShortUrlClicks;
    }

    public long getGrandTotalInteractions() {
        return grandTotalInteractions;
    }

    public void setGrandTotalInteractions(long grandTotalInteractions) {
        this.grandTotalInteractions = grandTotalInteractions;
    }

    public Map<String, Long> getDeviceDistribution() {
        return deviceDistribution;
    }

    public void setDeviceDistribution(Map<String, Long> deviceDistribution) {
        this.deviceDistribution = deviceDistribution;
    }

    public List<LinkItem> getTopLinks() {
        return topLinks;
    }

    public void setTopLinks(List<LinkItem> topLinks) {
        this.topLinks = topLinks;
    }

    public List<ShortUrl> getTopShortUrls() {
        return topShortUrls;
    }

    public void setTopShortUrls(List<ShortUrl> topShortUrls) {
        this.topShortUrls = topShortUrls;
    }

    public List<ClickEvent> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<ClickEvent> recentActivity) {
        this.recentActivity = recentActivity;
    }
}
