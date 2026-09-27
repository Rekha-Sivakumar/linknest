package com.linknest.controller;

import com.linknest.model.ClickEvent;
import com.linknest.model.LinkItem;
import com.linknest.model.Profile;
import com.linknest.model.ShortUrl;
import com.linknest.service.AnalyticsService;
import com.linknest.service.DataStorageService;
import com.linknest.service.QrCodeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Controller
@CrossOrigin(origins = "*")
public class PublicPageController {

    private final DataStorageService storageService;
    private final AnalyticsService analyticsService;
    private final QrCodeService qrCodeService;

    public PublicPageController(DataStorageService storageService, AnalyticsService analyticsService, QrCodeService qrCodeService) {
        this.storageService = storageService;
        this.analyticsService = analyticsService;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Public Bio-Page for a user: /p/{username}
     */
    @GetMapping("/p/{username}")
    @ResponseBody
    public ResponseEntity<String> getBioPage(@PathVariable String username, HttpServletRequest request) {
        Profile profile = storageService.getProfile(username);
        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<h1>404 - Profile @" + username + " Not Found</h1><p><a href='/'>Go to LinkNest Studio</a></p>");
        }

        // Track page view
        profile.setTotalPageViews(profile.getTotalPageViews() + 1);
        String device = analyticsService.detectDeviceType(request.getHeader("User-Agent"));
        String referrer = request.getHeader("Referer");
        storageService.recordClickEvent(new ClickEvent(
                UUID.randomUUID().toString(),
                "PROFILE_VIEW",
                profile.getUsername(),
                "Profile View @" + profile.getUsername(),
                device,
                referrer
        ));

        try {
            ClassPathResource resource = new ClassPathResource("static/bio.html");
            String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to load bio template.");
        }
    }

    /**
     * Short URL redirect endpoint: /r/{code}
     */
    @GetMapping("/r/{code}")
    public ResponseEntity<?> redirectShortUrl(@PathVariable String code, HttpServletRequest request) {
        ShortUrl shortUrl = storageService.getShortUrl(code);
        if (shortUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<h1>404 - Short Link Not Found</h1><p><a href='/'>Go to LinkNest Studio</a></p>");
        }

        // Track click
        shortUrl.setTotalClicks(shortUrl.getTotalClicks() + 1);
        String device = analyticsService.detectDeviceType(request.getHeader("User-Agent"));
        shortUrl.getClicksByDevice().put(device, shortUrl.getClicksByDevice().getOrDefault(device, 0L) + 1);

        String referrer = request.getHeader("Referer");
        storageService.recordClickEvent(new ClickEvent(
                UUID.randomUUID().toString(),
                "SHORT_URL",
                shortUrl.getCode(),
                shortUrl.getTitle(),
                device,
                referrer
        ));

        String target = shortUrl.getTargetUrl();
        if (!target.startsWith("http://") && !target.startsWith("https://")) {
            target = "https://" + target;
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(target))
                .build();
    }

    /**
     * Bio Link Click tracking redirect: /click/{linkId}
     */
    @GetMapping("/click/{linkId}")
    public ResponseEntity<?> clickBioLink(@PathVariable String linkId, HttpServletRequest request) {
        Profile profile = storageService.getPrimaryProfile();
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        LinkItem foundLink = null;
        for (LinkItem item : profile.getLinks()) {
            if (item.getId().equals(linkId)) {
                foundLink = item;
                break;
            }
        }

        if (foundLink == null) {
            return ResponseEntity.notFound().build();
        }

        // Increment clicks
        foundLink.setClicks(foundLink.getClicks() + 1);
        String device = analyticsService.detectDeviceType(request.getHeader("User-Agent"));
        String referrer = request.getHeader("Referer");
        storageService.recordClickEvent(new ClickEvent(
                UUID.randomUUID().toString(),
                "BIO_LINK",
                foundLink.getId(),
                foundLink.getTitle(),
                device,
                referrer
        ));

        String target = foundLink.getUrl();
        if (!target.startsWith("http://") && !target.startsWith("https://") && !target.startsWith("mailto:")) {
            target = "https://" + target;
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(target))
                .build();
    }

    /**
     * Dynamic QR Code endpoint: /api/qr
     */
    @GetMapping(value = "/api/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> getQrCode(
            @RequestParam("text") String text,
            @RequestParam(value = "size", defaultValue = "320") int size,
            @RequestParam(value = "fg", defaultValue = "6366F1") String fg,
            @RequestParam(value = "bg", defaultValue = "0F172A") String bg
    ) {
        try {
            int boundedSize = Math.max(120, Math.min(size, 800));
            byte[] imageBytes = qrCodeService.generateQrCode(text, boundedSize, boundedSize, fg, bg);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qrcode.png\"")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
