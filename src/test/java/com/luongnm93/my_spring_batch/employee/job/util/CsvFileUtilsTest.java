package com.luongnm93.my_spring_batch.employee.job.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvFileUtilsTest {

    @TempDir
    private Path tempDir;

    @Test
    void countDataLines_returnsDataLineCount() throws Exception {
        Path csv = tempDir.resolve("data.csv");
        Files.writeString(csv, "header\nrow1\nrow2\nrow3\n");

        assertThat(CsvFileUtils.countDataLines(csv.toString())).isEqualTo(3);
    }

    @Test
    void countDataLines_headerOnly_returnsZero() throws Exception {
        Path csv = tempDir.resolve("header-only.csv");
        Files.writeString(csv, "header\n");

        assertThat(CsvFileUtils.countDataLines(csv.toString())).isEqualTo(0);
    }

    @Test
    void countDataLines_fileNotFound_throwsIllegalStateException() {
        String missing = tempDir.resolve("nonexistent.csv").toString();

        assertThatThrownBy(() -> CsvFileUtils.countDataLines(missing))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot count lines");
    }
}
