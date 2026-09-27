package com.linknest.controller;

import com.linknest.model.ShortUrl;
import com.linknest.service.DataStorageService;
import com.linknest.service.QrCodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.*;

@Controller
@CrossOrigin(origins = "*")
public class UrlShortenerController {

    private final DataStorageService storageService;
    private final QrCodeService qrCodeService;

    public UrlShortenerController(DataStorageService storageService, QrCodeService qrCodeService) {
        this.storageService = storageService;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Render.com health check
     */
    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "LinkNest URL Shortener & QR Studio",
                "timestamp", Instant.now().toString(),
                "totalUrls", storageService.getAllShortUrls().size()
        ));
    }

    /**
     * List all shortened URLs
     */
    @GetMapping("/api/urls")
    @ResponseBody
    public ResponseEntity<Collection<ShortUrl>> getShortUrls() {
        return ResponseEntity.ok(storageService.getAllShortUrls());
    }

    /**
     * Create a shortened URL
     */
    @PostMapping("/api/urls")
    @ResponseBody
    public ResponseEntity<?> createShortUrl(@RequestBody Map<String, String> payload) {
        String targetUrl = payload.get("targetUrl");
        String customCode = payload.get("code");
        String title = payload.get("title");

        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Destination URL is required."));
        }

        String trimmedUrl = targetUrl.trim();
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            trimmedUrl = "https://" + trimmedUrl;
        }

        String code;
        if (customCode != null && !customCode.trim().isEmpty()) {
            code = customCode.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "");
            if (code.isEmpty()) {
                code = UUID.randomUUID().toString().substring(0, 6);
            }
        } else {
            code = UUID.randomUUID().toString().substring(0, 6);
        }

        if (storageService.getShortUrl(code) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Short code '/r/" + code + "' already exists. Choose another."));
        }

        ShortUrl shortUrl = new ShortUrl(code, trimmedUrl, title);
        storageService.saveShortUrl(shortUrl);
        return ResponseEntity.ok(shortUrl);
    }

    /**
     * Delete a shortened URL
     */
    @DeleteMapping("/api/urls/{code}")
    @ResponseBody
    public ResponseEntity<?> deleteShortUrl(@PathVariable String code) {
        boolean deleted = storageService.deleteShortUrl(code);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Deleted successfully."));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Redirect endpoint: /r/{code}
     */
    @GetMapping("/r/{code}")
    public ResponseEntity<?> redirect(@PathVariable String code) {
        ShortUrl shortUrl = storageService.getShortUrl(code);
        if (shortUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<h1>404 - Link Not Found</h1><p>The short link <code>/r/" + code + "</code> does not exist.</p><p><a href='/'>Go to LinkNest</a></p>");
        }

        storageService.incrementClicks(code);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(shortUrl.getTargetUrl()))
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
            @RequestParam(value = "bg", defaultValue = "FFFFFF") String bg
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
