package com.luongnm93.my_spring_batch.employee.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class GzFileStorageServiceImpl implements GzFileStorageService {

    private final Path uploadRoot;

    public GzFileStorageServiceImpl(@Value("${batch.upload.directory}") String uploadDirectory) throws IOException {
        this.uploadRoot = Path.of(uploadDirectory).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
    }

    /**
     * Stores the uploaded .gz file and returns its filename.
     *
     * @throws IllegalArgumentException if the file is not a .gz file or the filename is unsafe
     * @throws IOException              if writing to disk fails
     */
    @Override
    public String store(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".gz")) {
            throw new IllegalArgumentException("Only .gz files are accepted");
        }

        // Prevent directory traversal: strip any path components from the filename
        String safeFilename = Path.of(originalFilename).getFileName().toString();
        Path destination = uploadRoot.resolve(safeFilename).normalize();

        // Ensure the resolved destination is still inside the upload root
        if (!destination.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Filename resolves outside the upload directory");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Stored uploaded file: {}", destination);
        return safeFilename;
    }

    @Override
    public Path resolve(String fileName) {
        return uploadRoot.resolve(Path.of(fileName).getFileName()).normalize();
    }
}
