package com.luongnm93.my_spring_batch.employee.job;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CleanupTaskletTest {

    @TempDir
    Path tempDir;

    private ChunkContext mockChunkContext(String tempFilePath) {
        ExecutionContext jobCtx = new ExecutionContext();
        if (tempFilePath != null) {
            jobCtx.putString(DecompressTasklet.TEMP_FILE_PATH_KEY, tempFilePath);
        }

        org.springframework.batch.core.job.JobExecution jobExecution = mock(org.springframework.batch.core.job.JobExecution.class);
        when(jobExecution.getExecutionContext()).thenReturn(jobCtx);

        org.springframework.batch.core.step.StepExecution stepExecution = mock(org.springframework.batch.core.step.StepExecution.class);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);

        StepContext stepContext = mock(StepContext.class);
        when(stepContext.getStepExecution()).thenReturn(stepExecution);

        ChunkContext chunkContext = mock(ChunkContext.class);
        when(chunkContext.getStepContext()).thenReturn(stepContext);

        return chunkContext;
    }

    @Test
    void execute_existingTempFile_deletesFile() throws Exception {
        Path file = tempDir.resolve("temp.csv");
        Files.writeString(file, "data");
        assertThat(Files.exists(file)).isTrue();

        new CleanupTasklet().execute(mock(StepContribution.class), mockChunkContext(file.toString()));

        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void execute_nonExistentPath_noExceptionThrown() {
        String missing = tempDir.resolve("gone.csv").toString();

        assertThatNoException().isThrownBy(
                () -> new CleanupTasklet().execute(mock(StepContribution.class), mockChunkContext(missing))
        );
    }

    @Test
    void execute_nullTempPath_noExceptionThrown() {
        assertThatNoException().isThrownBy(
                () -> new CleanupTasklet().execute(mock(StepContribution.class), mockChunkContext(null))
        );
    }

    @Test
    void execute_returnsRepeatStatusFinished() throws Exception {
        RepeatStatus status = new CleanupTasklet().execute(mock(StepContribution.class), mockChunkContext(null));

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }
}
