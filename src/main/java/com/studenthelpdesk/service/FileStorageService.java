package com.studenthelpdesk.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create upload directory: " + this.uploadRoot, e);
        }
    }

    public String store(MultipartFile file) {
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String safe = StringUtils.cleanPath(original).replaceAll("[^A-Za-z0-9._-]", "_");
        if (safe.isBlank()) {
            safe = "file";
        }
        String stored = UUID.randomUUID() + "_" + safe;
        Path destination = uploadRoot.resolve(stored).normalize();

        if (!destination.startsWith(uploadRoot)) {
            throw new IllegalStateException("Invalid file path.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            return stored;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file. Please try a different file.", e);
        }
    }

    public Path resolve(String storedFileName) {
        Path resolved = uploadRoot.resolve(storedFileName).normalize();
        if (!resolved.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid stored file name.");
        }
        return resolved;
    }
}
