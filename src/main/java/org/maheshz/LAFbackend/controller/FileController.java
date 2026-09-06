package org.maheshz.LAFbackend.controller;

import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class FileController {

    private final FileService fileService;
    private static final long MAX_FILE_SIZE = 6 * 1024 * 1024; // 6MB in bytes

    // Allowed web-safe MIME types
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Uploaded file cannot be empty."));
        }

        // 1. Strict Size Verification (6MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("message", "File exceeds the 6MB maximum limit."));
        }

        // 2. Strict Format Verification
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid file type. Supported formats: JPG, PNG, WEBP, and PDF."));
        }

        String fileUrl = fileService.storeFile(file);
        return ResponseEntity.ok(Map.of(
                "url", fileUrl,
                "contentType", contentType,
                "size", String.valueOf(file.getSize())
        ));
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
            Path filePath = fileStorageLocation.resolve(fileName).normalize();

            if (!filePath.getParent().equals(fileStorageLocation)) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType;
                try {
                    contentType = Files.probeContentType(filePath);
                    if (contentType == null) contentType = "application/octet-stream";
                } catch (Exception e) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}