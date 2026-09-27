package com.linknest.controller;

import com.linknest.model.AnalyticsSummary;
import com.linknest.model.LinkItem;
import com.linknest.model.Profile;
import com.linknest.model.ShortUrl;
import com.linknest.service.AnalyticsService;
import com.linknest.service.DataStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private final DataStorageService storageService;
    private final AnalyticsService analyticsService;

    public ApiController(DataStorageService storageService, AnalyticsService analyticsService) {
        this.storageService = storageService;
        this.analyticsService = analyticsService;
    }

    /**
     * Render.com health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "UP");
        resp.put("service", "LinkNest Studio");
        resp.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(resp);
    }

    /**
     * Get primary profile for the studio
     */
    @GetMapping("/profile")
    public ResponseEntity<Profile> getProfile() {
        Profile profile = storageService.getPrimaryProfile();
        return ResponseEntity.ok(profile);
    }

    /**
     * Get profile by username
     */
    @GetMapping("/profile/{username}")
    public ResponseEntity<Profile> getProfileByUsername(@PathVariable String username) {
        Profile profile = storageService.getProfile(username);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    /**
     * Update profile details (name, bio, theme, avatar, socials)
     */
    @PutMapping("/profile")
    public ResponseEntity<Profile> updateProfile(@RequestBody Profile updated) {
        Profile profile = storageService.getPrimaryProfile();
        if (profile == null) {
            profile = updated;
        } else {
            if (updated.getDisplayName() != null) profile.setDisplayName(updated.getDisplayName());
            if (updated.getBio() != null) profile.setBio(updated.getBio());
            if (updated.getAvatarUrl() != null) profile.setAvatarUrl(updated.getAvatarUrl());
            if (updated.getTheme() != null) profile.setTheme(updated.getTheme());
            if (updated.getSocials() != null) profile.setSocials(updated.getSocials());
        }
        storageService.saveProfile(profile);
        return ResponseEntity.ok(profile);
    }

    /**
     * Add a link to the profile
     */
    @PostMapping("/links")
    public ResponseEntity<Profile> addLink(@RequestBody LinkItem item) {
        Profile profile = storageService.getPrimaryProfile();
        if (profile == null) return ResponseEntity.notFound().build();

        if (item.getId() == null || item.getId().isBlank()) {
            item.setId(UUID.randomUUID().toString());
        }
        profile.getLinks().add(item);
        storageService.saveProfile(profile);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update an existing link
     */
    @PutMapping("/links/{id}")
    public ResponseEntity<Profile> updateLink(@PathVariable String id, @RequestBody LinkItem item) {
        Profile profile = storageService.getPrimaryProfile();
        if (profile == null) return ResponseEntity.notFound().build();

        for (int i = 0; i < profile.getLinks().size(); i++) {
            LinkItem existing = profile.getLinks().get(i);
            if (existing.getId().equals(id)) {
                if (item.getTitle() != null) existing.setTitle(item.getTitle());
                if (item.getUrl() != null) existing.setUrl(item.getUrl());
                if (item.getIcon() != null) existing.setIcon(item.getIcon());
                existing.setEnabled(item.isEnabled());
                break;
            }
        }
        storageService.saveProfile(profile);
        return ResponseEntity.ok(profile);
    }

    /**
     * Delete a link from the profile
     */
    @DeleteMapping("/links/{id}")
    public ResponseEntity<Profile> deleteLink(@PathVariable String id) {
        Profile profile = storageService.getPrimaryProfile();
        if (profile == null) return ResponseEntity.notFound().build();

        profile.getLinks().removeIf(l -> l.getId().equals(id));
        storageService.saveProfile(profile);
        return ResponseEntity.ok(profile);
    }

    /**
     * List all shortened URLs
     */
    @GetMapping("/urls")
    public ResponseEntity<Collection<ShortUrl>> getShortUrls() {
        return ResponseEntity.ok(storageService.getAllShortUrls());
    }

    /**
     * Create a shortened URL
     */
    @PostMapping("/urls")
    public ResponseEntity<?> createShortUrl(@RequestBody Map<String, String> payload) {
        String targetUrl = payload.get("targetUrl");
        String customCode = payload.get("code");
        String title = payload.get("title");

        if (targetUrl == null || targetUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Target URL is required"));
        }

        String code;
        if (customCode != null && !customCode.trim().isEmpty()) {
            code = customCode.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "");
        } else {
            code = UUID.randomUUID().toString().substring(0, 6);
        }

        if (storageService.getShortUrl(code) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Short code '" + code + "' is already taken."));
        }

        ShortUrl shortUrl = new ShortUrl(code, targetUrl, title);
        storageService.saveShortUrl(shortUrl);
        return ResponseEntity.ok(shortUrl);
    }

    /**
     * Delete a shortened URL
     */
    @DeleteMapping("/urls/{code}")
    public ResponseEntity<?> deleteShortUrl(@PathVariable String code) {
        boolean deleted = storageService.deleteShortUrl(code);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Short URL deleted"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get aggregate analytics summary
     */
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsSummary> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }
}
