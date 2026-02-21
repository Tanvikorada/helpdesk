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
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create upload directory", e);
        }
    }

    public String store(MultipartFile file) {
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String safe = original.replaceAll("[^A-Za-z0-9._-]", "_");
        String stored = UUID.randomUUID() + "_" + safe;

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, uploadRoot.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
            return stored;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }
    }

    public Path resolve(String storedFileName) {
        return uploadRoot.resolve(storedFileName).normalize();
    }
}
