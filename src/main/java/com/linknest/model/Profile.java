package com.linknest.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile {
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String theme; // "midnight", "cyberpunk", "sunset", "emerald", "minimal"
    private List<LinkItem> links = new ArrayList<>();
    private Map<String, String> socials = new HashMap<>(); // "github", "linkedin", "twitter", "instagram", "email"
    private long totalPageViews = 0;

    public Profile() {}

    public Profile(String username, String displayName, String bio, String avatarUrl, String theme) {
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.theme = theme != null ? theme : "midnight";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public List<LinkItem> getLinks() {
        return links;
    }

    public void setLinks(List<LinkItem> links) {
        this.links = links;
    }

    public Map<String, String> getSocials() {
        return socials;
    }

    public void setSocials(Map<String, String> socials) {
        this.socials = socials;
    }

    public long getTotalPageViews() {
        return totalPageViews;
    }

    public void setTotalPageViews(long totalPageViews) {
        this.totalPageViews = totalPageViews;
    }
}
