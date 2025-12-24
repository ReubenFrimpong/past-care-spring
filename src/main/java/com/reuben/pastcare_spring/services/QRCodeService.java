package com.reuben.pastcare_spring.services;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Service for generating and validating QR codes for attendance check-in.
 *
 * Phase 1: Enhanced Attendance Tracking - QR Code System
 *
 * Security Features:
 * - AES-128 encryption of QR code payload
 * - Time-based expiry (default 24 hours)
 * - Session-specific codes (one code per session)
 * - Tamper detection via encryption
 */
@Service
public class QRCodeService {

    @Value("${qrcode.secret.key:PastCareQRCode16}")
    private String secretKey;

    @Value("${qrcode.default.expiry.hours:24}")
    private int defaultExpiryHours;

    private static final String ALGORITHM = "AES";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Generate encrypted QR code data for an attendance session.
     *
     * Format: {sessionId}:{expiryTimestamp}
     * This is then encrypted to prevent tampering.
     *
     * @param sessionId The attendance session ID
     * @return Encrypted QR code data string
     */
    public String generateQRCodeData(Long sessionId) {
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(defaultExpiryHours);
        String payload = sessionId + ":" + expiryTime.format(FORMATTER);

        return encrypt(payload);
    }

    /**
     * Generate encrypted QR code data with custom expiry time.
     *
     * @param sessionId The attendance session ID
     * @param expiryDateTime Custom expiry date and time
     * @return Encrypted QR code data string
     */
    public String generateQRCodeData(Long sessionId, LocalDateTime expiryDateTime) {
        String payload = sessionId + ":" + expiryDateTime.format(FORMATTER);
        return encrypt(payload);
    }

    /**
     * Validate and decrypt QR code data.
     *
     * @param qrCodeData The encrypted QR code data
     * @return Map with "valid" (Boolean), "sessionId" (Long), "expiry" (LocalDateTime), "message" (String)
     */
    public Map<String, Object> validateQRCode(String qrCodeData) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Decrypt the QR code data
            String decrypted = decrypt(qrCodeData);
            String[] parts = decrypted.split(":");

            if (parts.length != 2) {
                result.put("valid", false);
                result.put("message", "Invalid QR code format");
                return result;
            }

            Long sessionId = Long.parseLong(parts[0]);
            LocalDateTime expiryTime = LocalDateTime.parse(parts[1], FORMATTER);

            // Check if QR code has expired
            if (LocalDateTime.now().isAfter(expiryTime)) {
                result.put("valid", false);
                result.put("sessionId", sessionId);
                result.put("expiry", expiryTime);
                result.put("message", "QR code has expired");
                return result;
            }

            // QR code is valid
            result.put("valid", true);
            result.put("sessionId", sessionId);
            result.put("expiry", expiryTime);
            result.put("message", "QR code is valid");
            return result;

        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "QR code validation failed: " + e.getMessage());
            return result;
        }
    }

    /**
     * Generate QR code image as Base64 string.
     *
     * @param qrCodeData The data to encode in the QR code
     * @param width QR code width in pixels (default 300)
     * @param height QR code height in pixels (default 300)
     * @return Base64 encoded PNG image string
     */
    public String generateQRCodeImage(String qrCodeData, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(
                qrCodeData,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code image: " + e.getMessage(), e);
        }
    }

    /**
     * Generate QR code image with default size (300x300).
     *
     * @param qrCodeData The data to encode in the QR code
     * @return Base64 encoded PNG image string
     */
    public String generateQRCodeImage(String qrCodeData) {
        return generateQRCodeImage(qrCodeData, 300, 300);
    }

    /**
     * Encrypt a string using AES encryption.
     *
     * @param data The data to encrypt
     * @return Base64 encoded encrypted string
     */
    private String encrypt(String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt an encrypted string using AES decryption.
     *
     * @param encryptedData Base64 encoded encrypted string
     * @return Decrypted string
     */
    private String decrypt(String encryptedData) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);

            return new String(decrypted);

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a QR code is expired without full validation.
     *
     * @param qrCodeData The encrypted QR code data
     * @return true if expired, false otherwise
     */
    public boolean isExpired(String qrCodeData) {
        try {
            String decrypted = decrypt(qrCodeData);
            String[] parts = decrypted.split(":");

            if (parts.length != 2) {
                return true;
            }

            LocalDateTime expiryTime = LocalDateTime.parse(parts[1], FORMATTER);
            return LocalDateTime.now().isAfter(expiryTime);

        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extract session ID from QR code data without full validation.
     *
     * @param qrCodeData The encrypted QR code data
     * @return Session ID or null if extraction fails
     */
    public Long extractSessionId(String qrCodeData) {
        try {
            String decrypted = decrypt(qrCodeData);
            String[] parts = decrypted.split(":");

            if (parts.length != 2) {
                return null;
            }

            return Long.parseLong(parts[0]);

        } catch (Exception e) {
            return null;
        }
    }
}
