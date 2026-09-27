package com.linknest;

import com.linknest.model.AnalyticsSummary;
import com.linknest.model.LinkItem;
import com.linknest.model.Profile;
import com.linknest.model.ShortUrl;
import com.linknest.service.AnalyticsService;
import com.linknest.service.DataStorageService;
import com.linknest.service.QrCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinkNestServicesTest {

    private QrCodeService qrCodeService;
    private AnalyticsService analyticsService;
    private DataStorageService storageService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
        storageService = new DataStorageService();
        // Use test file path to avoid mutating main data
        try {
            java.lang.reflect.Field field = DataStorageService.class.getDeclaredField("dataFilePath");
            field.setAccessible(true);
            field.set(storageService, "./data/test-linknest-data.json");
        } catch (Exception ignored) {}
        analyticsService = new AnalyticsService(storageService);
    }

    @Test
    void testQrCodeGeneration() {
        byte[] png = qrCodeService.generateQrCode("https://linknest.app/p/alex", 300, 300, "6366F1", "FFFFFF");
        assertNotNull(png);
        assertTrue(png.length > 50, "QR Code byte array should not be empty");

        // Verify PNG magic numbers (0x89, 'P', 'N', 'G')
        assertEquals((byte) 0x89, png[0]);
        assertEquals((byte) 'P', png[1]);
        assertEquals((byte) 'N', png[2]);
        assertEquals((byte) 'G', png[3]);
    }

    @Test
    void testDeviceDetection() {
        String iphone = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148 Safari/604.1";
        assertEquals("Mobile", analyticsService.detectDeviceType(iphone));

        String ipad = "Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X) AppleWebKit/605.1.15";
        assertEquals("Tablet", analyticsService.detectDeviceType(ipad));

        String macChrome = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        assertEquals("Desktop", analyticsService.detectDeviceType(macChrome));
    }

    @Test
    void testDataStorageAndAnalytics() {
        Profile p = new Profile("testuser", "Test User", "Bio test", "", "midnight");
        LinkItem link = new LinkItem("link1", "Test Link", "https://example.com", "globe");
        link.setClicks(15);
        p.setLinks(List.of(link));
        p.setTotalPageViews(40);

        storageService.saveProfile(p);
        assertNotNull(storageService.getProfile("testuser"));

        ShortUrl su = new ShortUrl("tst", "https://example.com", "Test Short");
        su.setTotalClicks(25);
        storageService.saveShortUrl(su);
        assertNotNull(storageService.getShortUrl("tst"));

        AnalyticsSummary summary = analyticsService.getSummary();
        assertNotNull(summary);
        assertTrue(summary.getGrandTotalInteractions() >= 0);
    }
}
