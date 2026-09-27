package com.linknest.service;

import com.linknest.model.AnalyticsSummary;
import com.linknest.model.ClickEvent;
import com.linknest.model.LinkItem;
import com.linknest.model.Profile;
import com.linknest.model.ShortUrl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final DataStorageService storageService;

    public AnalyticsService(DataStorageService storageService) {
        this.storageService = storageService;
    }

    public String detectDeviceType(String userAgent) {
        if (userAgent == null) return "Desktop";
        String ua = userAgent.toLowerCase();
        if (ua.contains("ipad") || ua.contains("tablet") || (ua.contains("android") && !ua.contains("mobile"))) {
            return "Tablet";
        }
        if (ua.contains("mobile") || ua.contains("iphone") || ua.contains("android") || ua.contains("phone")) {
            return "Mobile";
        }
        return "Desktop";
    }

    public AnalyticsSummary getSummary() {
        AnalyticsSummary summary = new AnalyticsSummary();
        Profile profile = storageService.getPrimaryProfile();

        long pageViews = profile != null ? profile.getTotalPageViews() : 0;
        summary.setTotalPageViews(pageViews);

        long bioClicks = 0;
        List<LinkItem> allLinks = new ArrayList<>();
        if (profile != null && profile.getLinks() != null) {
            allLinks = new ArrayList<>(profile.getLinks());
            for (LinkItem link : allLinks) {
                bioClicks += link.getClicks();
            }
        }
        summary.setTotalBioLinkClicks(bioClicks);

        long shortClicks = 0;
        Collection<ShortUrl> shortUrls = storageService.getAllShortUrls();
        for (ShortUrl u : shortUrls) {
            shortClicks += u.getTotalClicks();
        }
        summary.setTotalShortUrlClicks(shortClicks);

        summary.setGrandTotalInteractions(pageViews + bioClicks + shortClicks);

        // Aggregate device distribution from ClickEvents
        Map<String, Long> devices = new HashMap<>();
        devices.put("Mobile", 0L);
        devices.put("Desktop", 0L);
        devices.put("Tablet", 0L);
        devices.put("Other", 0L);

        List<ClickEvent> events = storageService.getClickEvents();
        for (ClickEvent ev : events) {
            String dev = ev.getDeviceType();
            devices.put(dev, devices.getOrDefault(dev, 0L) + 1);
        }

        // Also incorporate shortUrls device metrics if events are sparse
        for (ShortUrl u : shortUrls) {
            for (Map.Entry<String, Long> entry : u.getClicksByDevice().entrySet()) {
                devices.put(entry.getKey(), devices.getOrDefault(entry.getKey(), 0L) + entry.getValue());
            }
        }

        summary.setDeviceDistribution(devices);

        // Top links
        allLinks.sort(Comparator.comparingLong(LinkItem::getClicks).reversed());
        summary.setTopLinks(allLinks.stream().limit(5).collect(Collectors.toList()));

        // Top short URLs
        List<ShortUrl> sortedUrls = new ArrayList<>(shortUrls);
        sortedUrls.sort(Comparator.comparingLong(ShortUrl::getTotalClicks).reversed());
        summary.setTopShortUrls(sortedUrls.stream().limit(5).collect(Collectors.toList()));

        // Recent activity (latest 15)
        summary.setRecentActivity(events.stream().limit(15).collect(Collectors.toList()));

        return summary;
    }
}
