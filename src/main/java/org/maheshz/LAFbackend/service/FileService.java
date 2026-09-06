package org.maheshz.LAFbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    // SECURITY FIX: Strict Whitelist for allowed extensions
    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".webp");
    private final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/jpeg", "image/png", "image/webp");

    public FileService() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the storage directory.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            // 1. Validate MIME Type
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
                throw new SecurityException("Invalid file type. Only JPG, PNG, and WEBP images are allowed.");
            }

            // 2. Validate Extension
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName != null && originalFileName.contains(".")
                    ? originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase()
                    : "";

            if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                throw new SecurityException("Invalid file extension.");
            }

            // 3. Generate secure random filename
            String newFileName = UUID.randomUUID() + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(newFileName).normalize();

            // SECURITY FIX: Path Traversal Guard
            if (!targetLocation.getParent().equals(this.fileStorageLocation)) {
                throw new SecurityException("Path traversal attempt detected. Cannot store file outside target directory.");
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/files/")
                    .path(newFileName)
                    .toUriString();
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }
}