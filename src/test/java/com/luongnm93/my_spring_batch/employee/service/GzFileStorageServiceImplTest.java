package com.luongnm93.my_spring_batch.employee.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GzFileStorageServiceImplTest {

    @TempDir
    Path uploadRoot;

    private GzFileStorageServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new GzFileStorageServiceImpl(uploadRoot.toString());
    }

    private MultipartFile mockFile(String originalFilename, byte[] content) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        return file;
    }

    @Test
    void store_validGzFile_copiesFileAndReturnsFileName() throws Exception {
        MultipartFile file = mockFile("employees.csv.gz", "gz-content".getBytes());

        String returned = service.store(file);

        assertThat(returned).isEqualTo("employees.csv.gz");
        assertThat(Files.exists(uploadRoot.resolve("employees.csv.gz"))).isTrue();
    }

    @Test
    void store_nonGzFile_throwsIllegalArgumentException() throws Exception {
        MultipartFile file = mockFile("employees.csv", "content".getBytes());

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(".gz");
    }

    @Test
    void store_nullFilename_throwsIllegalArgumentException() throws Exception {
        MultipartFile file = mockFile(null, "content".getBytes());

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void store_directoryTraversalFilename_sanitizedAndStoredInsideRoot() throws Exception {
        // "../other/employees.csv.gz" → stripped to "employees.csv.gz", stored under uploadRoot
        MultipartFile file = mockFile("../other/employees.csv.gz", "gz-content".getBytes());

        String returned = service.store(file);

        assertThat(returned).isEqualTo("employees.csv.gz");
        assertThat(Files.exists(uploadRoot.resolve("employees.csv.gz"))).isTrue();
    }

    @Test
    void store_existingFile_overwritesFile() throws Exception {
        Path existing = uploadRoot.resolve("employees.csv.gz");
        Files.writeString(existing, "old content");

        MultipartFile file = mockFile("employees.csv.gz", "new content".getBytes());
        service.store(file);

        assertThat(Files.readString(existing)).isEqualTo("new content");
    }

    @Test
    void resolve_simpleFileName_returnsPathUnderUploadRoot() {
        Path resolved = service.resolve("employees.csv.gz");

        assertThat(resolved.startsWith(uploadRoot)).isTrue();
        assertThat(resolved.getFileName().toString()).isEqualTo("employees.csv.gz");
    }

    @Test
    void resolve_fileNameWithDirectoryComponent_stripsPathAndResolvesInsideRoot() {
        Path resolved = service.resolve("../other/employees.csv.gz");

        assertThat(resolved.startsWith(uploadRoot)).isTrue();
        assertThat(resolved.getFileName().toString()).isEqualTo("employees.csv.gz");
    }
}
