package com.linknest.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrCodeService {

    /**
     * Generates a branded PNG QR code with customizable colors and size.
     *
     * @param text     The URL or text to encode
     * @param width    Width in pixels (e.g. 350)
     * @param height   Height in pixels (e.g. 350)
     * @param fgHex    Foreground ARGB/RGB hex color (e.g. "6366F1" or "000000")
     * @param bgHex    Background ARGB/RGB hex color (e.g. "FFFFFF" or "0F172A")
     * @return PNG image bytes
     */
    public byte[] generateQrCode(String text, int width, int height, String fgHex, String bgHex) {
        try {
            int onColor = parseColor(fgHex, 0xFF000000);
            int offColor = parseColor(bgHex, 0xFFFFFFFF);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            MatrixToImageConfig config = new MatrixToImageConfig(onColor, offColor);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream, config);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }

    private int parseColor(String hex, int defaultColor) {
        if (hex == null || hex.isBlank()) {
            return defaultColor;
        }
        try {
            String clean = hex.replace("#", "").trim();
            if (clean.length() == 6) {
                // Add alpha 0xFF
                long val = Long.parseLong(clean, 16);
                return (int) (0xFF000000L | val);
            } else if (clean.length() == 8) {
                return (int) Long.parseLong(clean, 16);
            }
        } catch (Exception ignored) {}
        return defaultColor;
    }
}
