package com.reuben.pastcare_spring.services;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${app.upload.dir:uploads/profile-images}")
    private String uploadDir;

    @Value("${app.upload.fellowship-dir:uploads/fellowship-images}")
    private String fellowshipUploadDir;

    @Value("${app.upload.event-dir:uploads/event-images}")
    private String eventUploadDir;

    @Value("${app.upload.max-size-kb:100}")
    private int maxSizeKb;

    /**
     * Upload and compress a profile image
     * @param file The image file to upload
     * @param oldImagePath The path to the old image (to delete)
     * @return The relative path to the saved image
     */
    public String uploadProfileImage(MultipartFile file, String oldImagePath) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);

        // Compress image to target size
        byte[] compressedImage = compressImage(file.getBytes(), maxSizeKb);

        // Save compressed image
        Files.write(filePath, compressedImage);

        // Delete old image if exists
        if (oldImagePath != null && !oldImagePath.isEmpty()) {
            deleteImage(oldImagePath);
        }

        // Return relative path
        return uploadDir + "/" + filename;
    }

    /**
     * Delete an image file
     */
    public void deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }

        try {
            Path path = Paths.get(imagePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Failed to delete image: " + imagePath);
        }
    }

    /**
     * Compress image to target size in KB
     */
    private byte[] compressImage(byte[] imageBytes, int targetSizeKb) throws IOException {
        // Start with high quality
        double quality = 0.9;
        byte[] result = imageBytes;

        // Iteratively reduce quality until target size is reached
        while (result.length > targetSizeKb * 1024 && quality > 0.1) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(new java.io.ByteArrayInputStream(imageBytes))
                    .scale(1.0)
                    .outputQuality(quality)
                    .toOutputStream(outputStream);

            result = outputStream.toByteArray();
            quality -= 0.1;
        }

        // If still too large, resize the image
        if (result.length > targetSizeKb * 1024) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(new java.io.ByteArrayInputStream(imageBytes))
                    .size(800, 800) // Max dimensions
                    .outputQuality(0.8)
                    .toOutputStream(outputStream);

            result = outputStream.toByteArray();
        }

        return result;
    }

    /**
     * Upload and compress a fellowship image
     * @param file The image file to upload
     * @param oldImagePath The path to the old image (to delete)
     * @return The relative path to the saved image
     */
    public String uploadFellowshipImage(MultipartFile file, String oldImagePath) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(fellowshipUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);

        // Compress image to target size (allow larger size for fellowship images - 500KB)
        byte[] compressedImage = compressImage(file.getBytes(), 500);

        // Save compressed image
        Files.write(filePath, compressedImage);

        // Delete old image if exists
        if (oldImagePath != null && !oldImagePath.isEmpty()) {
            deleteImage(oldImagePath);
        }

        // Return relative path
        return fellowshipUploadDir + "/" + filename;
    }

    /**
     * Upload and compress an event image
     * @param file The image file to upload
     * @param oldImagePath The path to the old image (to delete)
     * @return The relative path to the saved image
     */
    public String uploadEventImage(MultipartFile file, String oldImagePath) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(eventUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);

        // Compress image to target size (allow larger size for event images - 500KB)
        byte[] compressedImage = compressImage(file.getBytes(), 500);

        // Save compressed image
        Files.write(filePath, compressedImage);

        // Delete old image if exists
        if (oldImagePath != null && !oldImagePath.isEmpty()) {
            deleteImage(oldImagePath);
        }

        // Return relative path
        return eventUploadDir + "/" + filename;
    }
}
