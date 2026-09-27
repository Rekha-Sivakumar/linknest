package com.linknest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.linknest.model.ClickEvent;
import com.linknest.model.LinkItem;
import com.linknest.model.Profile;
import com.linknest.model.ShortUrl;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class DataStorageService {

    @Value("${linknest.data-file:./data/linknest-data.json}")
    private String dataFilePath = "./data/linknest-data.json";

    private final ObjectMapper mapper;
    private final Map<String, Profile> profiles = new ConcurrentHashMap<>();
    private final Map<String, ShortUrl> shortUrls = new ConcurrentHashMap<>();
    private final List<ClickEvent> clickEvents = new CopyOnWriteArrayList<>();

    public DataStorageService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        loadData();
        if (profiles.isEmpty() || !profiles.containsKey("alex")) {
            seedDemoData();
            saveData();
        }
    }

    public synchronized void saveData() {
        try {
            Path path = Paths.get(dataFilePath);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("profiles", profiles);
            data.put("shortUrls", shortUrls);
            data.put("clickEvents", clickEvents);

            mapper.writeValue(new File(dataFilePath), data);
        } catch (IOException e) {
            System.err.println("Warning: Could not save LinkNest data: " + e.getMessage());
        }
    }

    private void loadData() {
        File file = new File(dataFilePath);
        if (!file.exists()) {
            return;
        }

        try {
            Map<String, Object> data = mapper.readValue(file, new TypeReference<>() {});
            if (data.containsKey("profiles")) {
                Map<String, Object> profMap = (Map<String, Object>) data.get("profiles");
                for (Map.Entry<String, Object> entry : profMap.entrySet()) {
                    Profile p = mapper.convertValue(entry.getValue(), Profile.class);
                    profiles.put(entry.getKey().toLowerCase(), p);
                }
            }
            if (data.containsKey("shortUrls")) {
                Map<String, Object> urlMap = (Map<String, Object>) data.get("shortUrls");
                for (Map.Entry<String, Object> entry : urlMap.entrySet()) {
                    ShortUrl u = mapper.convertValue(entry.getValue(), ShortUrl.class);
                    shortUrls.put(entry.getKey().toLowerCase(), u);
                }
            }
            if (data.containsKey("clickEvents")) {
                List<?> events = (List<?>) data.get("clickEvents");
                for (Object ev : events) {
                    ClickEvent event = mapper.convertValue(ev, ClickEvent.class);
                    clickEvents.add(event);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not parse existing data file, seeding fresh: " + e.getMessage());
        }
    }

    public Profile getProfile(String username) {
        if (username == null) return null;
        return profiles.get(username.toLowerCase().trim());
    }

    public Profile getPrimaryProfile() {
        // Return first or "alex"
        if (profiles.containsKey("alex")) {
            return profiles.get("alex");
        }
        if (!profiles.isEmpty()) {
            return profiles.values().iterator().next();
        }
        return null;
    }

    public synchronized void saveProfile(Profile profile) {
        if (profile == null || profile.getUsername() == null) return;
        profiles.put(profile.getUsername().toLowerCase().trim(), profile);
        saveData();
    }

    public Collection<ShortUrl> getAllShortUrls() {
        return shortUrls.values();
    }

    public ShortUrl getShortUrl(String code) {
        if (code == null) return null;
        return shortUrls.get(code.toLowerCase().trim());
    }

    public synchronized void saveShortUrl(ShortUrl shortUrl) {
        if (shortUrl == null || shortUrl.getCode() == null) return;
        shortUrls.put(shortUrl.getCode().toLowerCase().trim(), shortUrl);
        saveData();
    }

    public synchronized boolean deleteShortUrl(String code) {
        if (code == null) return false;
        boolean removed = shortUrls.remove(code.toLowerCase().trim()) != null;
        if (removed) {
            saveData();
        }
        return removed;
    }

    public synchronized void recordClickEvent(ClickEvent event) {
        clickEvents.add(0, event);
        // Limit historical events in memory/disk to 500
        while (clickEvents.size() > 500) {
            clickEvents.remove(clickEvents.size() - 1);
        }
        saveData();
    }

    public List<ClickEvent> getClickEvents() {
        return new ArrayList<>(clickEvents);
    }

    private void seedDemoData() {
        Profile alex = new Profile(
                "alex",
                "Alex Chen",
                "Full-Stack Software Engineer • Java, Spring Boot & Cloud Enthusiast 🚀 Building open-source tools.",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80",
                "cyberpunk"
        );
        alex.setTotalPageViews(142);

        List<LinkItem> links = new ArrayList<>();
        LinkItem l1 = new LinkItem(UUID.randomUUID().toString(), "Personal Portfolio & Projects", "https://github.com", "globe");
        l1.setClicks(84);
        LinkItem l2 = new LinkItem(UUID.randomUUID().toString(), "My GitHub Repositories", "https://github.com", "github");
        l2.setClicks(126);
        LinkItem l3 = new LinkItem(UUID.randomUUID().toString(), "Connect on LinkedIn", "https://linkedin.com", "linkedin");
        l3.setClicks(65);
        LinkItem l4 = new LinkItem(UUID.randomUUID().toString(), "Read My Tech Articles", "https://medium.com", "newspaper");
        l4.setClicks(39);
        LinkItem l5 = new LinkItem(UUID.randomUUID().toString(), "YouTube Coding Tutorials", "https://youtube.com", "youtube");
        l5.setClicks(91);

        links.add(l1);
        links.add(l2);
        links.add(l3);
        links.add(l4);
        links.add(l5);
        alex.setLinks(links);

        Map<String, String> socials = new HashMap<>();
        socials.put("github", "https://github.com");
        socials.put("linkedin", "https://linkedin.com");
        socials.put("twitter", "https://twitter.com");
        socials.put("email", "mailto:alex@example.com");
        alex.setSocials(socials);

        profiles.put("alex", alex);

        // Seed sample short URLs
        ShortUrl s1 = new ShortUrl("github", "https://github.com", "My GitHub Profile");
        s1.setTotalClicks(97);
        s1.getClicksByDevice().put("Mobile", 45L);
        s1.getClicksByDevice().put("Desktop", 48L);
        s1.getClicksByDevice().put("Tablet", 4L);

        ShortUrl s2 = new ShortUrl("linkedin", "https://linkedin.com", "LinkedIn Connect");
        s2.setTotalClicks(52);
        s2.getClicksByDevice().put("Mobile", 30L);
        s2.getClicksByDevice().put("Desktop", 22L);

        shortUrls.put("github", s1);
        shortUrls.put("linkedin", s2);

        // Seed some sample recent click events
        clickEvents.add(new ClickEvent(UUID.randomUUID().toString(), "BIO_LINK", l2.getId(), "My GitHub Repositories", "Desktop", "Google"));
        clickEvents.add(new ClickEvent(UUID.randomUUID().toString(), "BIO_LINK", l1.getId(), "Personal Portfolio & Projects", "Mobile", "Instagram"));
        clickEvents.add(new ClickEvent(UUID.randomUUID().toString(), "SHORT_URL", "github", "My GitHub Profile", "Mobile", "Twitter"));
        clickEvents.add(new ClickEvent(UUID.randomUUID().toString(), "BIO_LINK", l5.getId(), "YouTube Coding Tutorials", "Desktop", "Direct"));
    }
}
