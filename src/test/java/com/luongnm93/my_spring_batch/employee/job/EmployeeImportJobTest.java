package com.luongnm93.my_spring_batch.employee.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class EmployeeImportJobTest {

    private static final String TEST_FILE_PATH;

    static {
        try {
            TEST_FILE_PATH = new ClassPathResource("test-employees.csv.gz").getFile().getAbsolutePath();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jobRepositoryTestUtils.removeJobExecutions();
        jdbcTemplate.execute("DELETE FROM employees");
    }

    @Test
    void jobCompletesSuccessfully() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("filePath", TEST_FILE_PATH)
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void jobInsertsAllRowsFromCsv() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("filePath", TEST_FILE_PATH)
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees", Integer.class);
        assertThat(count).isEqualTo(3);
    }

    @Test
    void jobMapsFieldsCorrectly() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("filePath", TEST_FILE_PATH)
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        String firstName = jdbcTemplate.queryForObject(
                "SELECT first_name FROM employees WHERE employee_id = 'EMP-000001'", String.class);
        assertThat(firstName).isEqualTo("John");

        String managerId = jdbcTemplate.queryForObject(
                "SELECT manager_id FROM employees WHERE employee_id = 'EMP-000001'", String.class);
        assertThat(managerId).isNull();

        String managerIdOfEmp2 = jdbcTemplate.queryForObject(
                "SELECT manager_id FROM employees WHERE employee_id = 'EMP-000002'", String.class);
        assertThat(managerIdOfEmp2).isEqualTo("EMP-000001");
    }

    @Test
    void jobWritesReadCountToStepExecution() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("filePath", TEST_FILE_PATH)
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        long readCount = execution.getStepExecutions().stream()
                .filter(se -> se.getStepName().contains(":partition"))
                .mapToLong(se -> se.getReadCount())
                .sum();
        assertThat(readCount).isEqualTo(3);
    }
}
