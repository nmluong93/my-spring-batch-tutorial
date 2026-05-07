package com.luongnm93.my_spring_batch.employee.job;

import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.nio.file.Files;
import java.nio.file.Path;

public class CleanupTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String tempPath = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext()
                .getString(DecompressTasklet.TEMP_FILE_PATH_KEY);

        if (tempPath != null) {
            Files.deleteIfExists(Path.of(tempPath));
        }

        return RepeatStatus.FINISHED;
    }
}
