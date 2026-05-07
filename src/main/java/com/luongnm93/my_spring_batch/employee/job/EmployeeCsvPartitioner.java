package com.luongnm93.my_spring_batch.employee.job;

import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class EmployeeCsvPartitioner implements Partitioner {

    private final int totalLines;

    public EmployeeCsvPartitioner(int totalLines) {
        this.totalLines = totalLines;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int linesPerPartition = totalLines / gridSize;
        int remainder = totalLines % gridSize;

        Map<String, ExecutionContext> partitions = new HashMap<>();

        int currentStart = 1;
        for (int i = 0; i < gridSize; i++) {
            int count = linesPerPartition + (i < remainder ? 1 : 0);

            ExecutionContext context = new ExecutionContext();
            context.putInt("startLine", currentStart);
            context.putInt("lineCount", count);

            partitions.put("partition" + i, context);
            currentStart += count;
        }

        return partitions;
    }
}
