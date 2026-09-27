package com.linknest.model;

public class LinkItem {
    private String id;
    private String title;
    private String url;
    private String icon; // e.g. "github", "linkedin", "globe", "youtube", "twitter", "star"
    private long clicks;
    private boolean enabled = true;

    public LinkItem() {}

    public LinkItem(String id, String title, String url, String icon) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.icon = icon != null ? icon : "globe";
        this.clicks = 0;
        this.enabled = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public long getClicks() {
        return clicks;
    }

    public void setClicks(long clicks) {
        this.clicks = clicks;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
