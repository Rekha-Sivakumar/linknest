package com.linknest;

import com.linknest.model.ShortUrl;
import com.linknest.service.DataStorageService;
import com.linknest.service.QrCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkNestServicesTest {

    private QrCodeService qrCodeService;
    private DataStorageService storageService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
        storageService = new DataStorageService();
        try {
            java.lang.reflect.Field field = DataStorageService.class.getDeclaredField("dataFilePath");
            field.setAccessible(true);
            field.set(storageService, "./data/test-linknest-urls.json");
        } catch (Exception ignored) {}
    }

    @Test
    void testQrCodeGeneration() {
        byte[] png = qrCodeService.generateQrCode("https://example.com/test", 300, 300, "6366F1", "FFFFFF");
        assertNotNull(png);
        assertTrue(png.length > 50, "QR Code byte array should not be empty");

        // Verify PNG magic numbers (0x89, 'P', 'N', 'G')
        assertEquals((byte) 0x89, png[0]);
        assertEquals((byte) 'P', png[1]);
        assertEquals((byte) 'N', png[2]);
        assertEquals((byte) 'G', png[3]);
    }

    @Test
    void testShortUrlStorageAndClicks() {
        ShortUrl url = new ShortUrl("sample", "https://example.com/my-page", "Sample Page");
        storageService.saveShortUrl(url);

        ShortUrl retrieved = storageService.getShortUrl("sample");
        assertNotNull(retrieved);
        assertEquals("https://example.com/my-page", retrieved.getTargetUrl());
        assertEquals(0, retrieved.getTotalClicks());

        storageService.incrementClicks("sample");
        assertEquals(1, storageService.getShortUrl("sample").getTotalClicks());

        boolean deleted = storageService.deleteShortUrl("sample");
        assertTrue(deleted);
        assertNull(storageService.getShortUrl("sample"));
    }
}
