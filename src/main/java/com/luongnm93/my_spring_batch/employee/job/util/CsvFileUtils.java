package com.luongnm93.my_spring_batch.employee.job.util;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public final class CsvFileUtils {

    public static int countDataLines(String filePath) {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {
            int count = 0;
            while (reader.readLine() != null) count++;
            return count - 1; // subtract header
        } catch (Exception e) {
            throw new IllegalStateException("Cannot count lines in " + filePath, e);
        }
    }
}
