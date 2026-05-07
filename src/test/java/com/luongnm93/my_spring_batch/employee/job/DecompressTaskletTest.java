package com.luongnm93.my_spring_batch.employee.job;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DecompressTaskletTest {

    @TempDir
    static Path tempDir;

    private static final String SHARED_CONTENT = "header\nrow1\nrow2\n";
    private Path sharedGz;

    @BeforeAll
    void setup() throws IOException {
        sharedGz = tempDir.resolve("employees.csv.gz");
        try (OutputStream fos = Files.newOutputStream(sharedGz);
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
            gzos.write(SHARED_CONTENT.getBytes());
        }
    }

    private ChunkContext mockChunkContextWithJobExecutionContext(ExecutionContext jobExecutionContext) {
        org.springframework.batch.core.job.JobExecution jobExecution = mock(org.springframework.batch.core.job.JobExecution.class);
        when(jobExecution.getExecutionContext()).thenReturn(jobExecutionContext);

        org.springframework.batch.core.step.StepExecution stepExecution = mock(org.springframework.batch.core.step.StepExecution.class);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);

        StepContext stepContext = mock(StepContext.class);
        when(stepContext.getStepExecution()).thenReturn(stepExecution);

        ChunkContext chunkContext = mock(ChunkContext.class);
        when(chunkContext.getStepContext()).thenReturn(stepContext);

        return chunkContext;
    }

    @Test
    void execute_validGzFile_storesTempFilePathInContext() throws Exception {
        ExecutionContext jobCtx = new ExecutionContext();
        ChunkContext chunkContext = mockChunkContextWithJobExecutionContext(jobCtx);

        new DecompressTasklet(sharedGz.toString()).execute(mock(StepContribution.class), chunkContext);

        String storedPath = jobCtx.getString(DecompressTasklet.TEMP_FILE_PATH_KEY);
        assertThat(storedPath).isNotNull();
        assertThat(Files.exists(Path.of(storedPath))).isTrue();
    }

    @Test
    void execute_validGzFile_decompressedContentIsCorrect() throws Exception {
        ExecutionContext jobCtx = new ExecutionContext();
        ChunkContext chunkContext = mockChunkContextWithJobExecutionContext(jobCtx);

        new DecompressTasklet(sharedGz.toString()).execute(mock(StepContribution.class), chunkContext);

        String storedPath = jobCtx.getString(DecompressTasklet.TEMP_FILE_PATH_KEY);
        assertThat(Files.readString(Path.of(storedPath))).isEqualTo(SHARED_CONTENT);
    }

    @Test
    void execute_returnsRepeatStatusFinished() throws Exception {
        ExecutionContext jobCtx = new ExecutionContext();
        ChunkContext chunkContext = mockChunkContextWithJobExecutionContext(jobCtx);

        RepeatStatus status = new DecompressTasklet(sharedGz.toString()).execute(mock(StepContribution.class), chunkContext);

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    void execute_fileNotFound_throwsIOException() {
        String missing = tempDir.resolve("nonexistent.csv.gz").toString();
        ExecutionContext jobCtx = new ExecutionContext();
        ChunkContext chunkContext = mockChunkContextWithJobExecutionContext(jobCtx);

        assertThatThrownBy(() -> new DecompressTasklet(missing).execute(mock(StepContribution.class), chunkContext))
                .isInstanceOf(IOException.class);
    }

    @Test
    void execute_invalidGzContent_throwsIOException() throws Exception {
        Path badGz = tempDir.resolve("bad.csv.gz");
        Files.writeString(badGz, "not gzip content");
        ExecutionContext jobCtx = new ExecutionContext();
        ChunkContext chunkContext = mockChunkContextWithJobExecutionContext(jobCtx);

        assertThatThrownBy(() -> new DecompressTasklet(badGz.toString()).execute(mock(StepContribution.class), chunkContext))
                .isInstanceOf(IOException.class);
    }
}
