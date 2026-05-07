package com.luongnm93.my_spring_batch.employee.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface GzFileStorageService {
    String store(MultipartFile file) throws IOException;

    Path resolve(String fileName);
}
