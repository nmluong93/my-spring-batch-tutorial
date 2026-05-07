package com.luongnm93.my_spring_batch.employee.job;

import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class DecompressTasklet implements Tasklet {

    static final String TEMP_FILE_PATH_KEY = "decompressed.tempFilePath";

    private final String filePath;

    public DecompressTasklet(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws IOException {
        String baseName = Path.of(filePath).getFileName().toString().replace(".gz", "");
        Path tempFile = Files.createTempFile("employee-import-", "-" + baseName);

        try (InputStream raw = Files.newInputStream(Path.of(filePath));
             GZIPInputStream gz = new GZIPInputStream(raw)) {
            Files.copy(gz, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        ExecutionContext jobContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();
        jobContext.putString(TEMP_FILE_PATH_KEY, tempFile.toAbsolutePath().toString());

        return RepeatStatus.FINISHED;
    }
}
