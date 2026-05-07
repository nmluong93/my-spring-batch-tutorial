package com.luongnm93.my_spring_batch.employee.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeCsvPartitionerTest {

    @Test
    void partition_returnsExpectedNumberOfPartitions() {
        Map<String, ExecutionContext> result = new EmployeeCsvPartitioner(50).partition(5);

        assertThat(result).hasSize(5);
    }

    @Test
    void partition_evenDivision_allPartitionsHaveEqualLineCount() {
        Map<String, ExecutionContext> result = new EmployeeCsvPartitioner(100).partition(10);

        result.values().forEach(ctx -> assertThat(ctx.getInt("lineCount")).isEqualTo(10));
    }

    @Test
    void partition_unevenDivision_remainderDistributedToEarlyPartitions() {
        Map<String, ExecutionContext> result = new EmployeeCsvPartitioner(103).partition(10);

        long bigPartitions = result.values().stream()
                .filter(ctx -> ctx.getInt("lineCount") == 11)
                .count();
        long smallPartitions = result.values().stream()
                .filter(ctx -> ctx.getInt("lineCount") == 10)
                .count();

        assertThat(bigPartitions).isEqualTo(3);
        assertThat(smallPartitions).isEqualTo(7);
    }

    @Test
    void partition_correctStartLineSequence_contiguousAndNoOverlap() {
        int totalLines = 103;
        int gridSize = 10;
        Map<String, ExecutionContext> result = new EmployeeCsvPartitioner(totalLines).partition(gridSize);

        int coveredLines = result.values().stream()
                .mapToInt(ctx -> ctx.getInt("lineCount"))
                .sum();

        assertThat(coveredLines).isEqualTo(totalLines);

        // verify startLines are sequential (each partition starts right after previous ends)
        int expectedStart = 1;
        for (int i = 0; i < gridSize; i++) {
            ExecutionContext ctx = result.get("partition" + i);
            assertThat(ctx.getInt("startLine")).isEqualTo(expectedStart);
            expectedStart += ctx.getInt("lineCount");
        }
    }

    @Test
    void partition_singlePartition_getsAllLines() {
        Map<String, ExecutionContext> result = new EmployeeCsvPartitioner(50).partition(1);

        assertThat(result).hasSize(1);
        ExecutionContext ctx = result.get("partition0");
        assertThat(ctx.getInt("startLine")).isEqualTo(1);
        assertThat(ctx.getInt("lineCount")).isEqualTo(50);
    }
}
