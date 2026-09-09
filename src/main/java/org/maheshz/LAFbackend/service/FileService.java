package org.maheshz.LAFbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    // Spring will automatically inject the Bean from CloudinaryConfig
    private final Cloudinary cloudinary;

    public String storeFile(MultipartFile multipartFile) {
        File tempFile = null;
        try {
            // 1. Enterprise Standard: Stream to a temp disk file to prevent RAM crashes (OOM)
            String originalFilename = multipartFile.getOriginalFilename() != null ? multipartFile.getOriginalFilename() : "unknown";
            tempFile = File.createTempFile("laf_upload_", "_" + originalFilename);

            try (var inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // 2. Upload to Cloudinary with secure random naming
            Map<?, ?> uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.asMap(
                    "resource_type", "auto",
                    "folder", "lost-and-found-nepal",
                    "use_filename", false,
                    "unique_filename", true // Security: Prevents file overwrites & URL guessing
            ));

            String secureUrl = uploadResult.get("secure_url").toString();
            log.info("[FILE AUDIT] Successfully uploaded file to Cloudinary. URL: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("[FILE AUDIT] Cloudinary upload failed due to I/O error", e);
            throw new RuntimeException("Failed to upload file to cloud storage. Please try again.");
        } finally {
            // 3. Strict Cleanup: Always delete the temporary file from the server to prevent storage leaks
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    log.warn("[FILE AUDIT] Storage Warning: Failed to delete temporary file at {}", tempFile.getAbsolutePath());
                }
            }
        }
    }
}