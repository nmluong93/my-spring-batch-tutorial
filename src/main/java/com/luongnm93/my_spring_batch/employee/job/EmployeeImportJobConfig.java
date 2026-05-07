package com.luongnm93.my_spring_batch.employee.job;

import com.luongnm93.my_spring_batch.employee.dto.EmployeeCsvDto;
import com.luongnm93.my_spring_batch.employee.entity.Employee;
import com.luongnm93.my_spring_batch.employee.job.util.CsvFileUtils;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class EmployeeImportJobConfig {

    private static final int CHUNK_SIZE = 1000;

    @Value("${batch.partition.grid-size:10}")
    private int gridSize;

    // ------------------------------------------------------------------
    // Reader — scoped per partition, reads from the decompressed temp file
    // ------------------------------------------------------------------

    @Bean
    @StepScope
    public FlatFileItemReader<EmployeeCsvDto> employeeItemReader(
            @Value("#{jobExecutionContext['" + DecompressTasklet.TEMP_FILE_PATH_KEY + "']}") String tempFilePath,
            @Value("#{stepExecutionContext['startLine']}") int startLine,
            @Value("#{stepExecutionContext['lineCount']}") int lineCount
    ) {
        FlatFileItemReader<EmployeeCsvDto> reader = new FlatFileItemReaderBuilder<EmployeeCsvDto>()
                .name("employeeItemReader")
                .resource(new FileSystemResource(tempFilePath))
                .linesToSkip(startLine)
                .delimited()
                .names(
                        "employeeId", "firstName", "lastName", "email", "phone",
                        "department", "jobTitle", "employmentType", "hireDate", "salary",
                        "managerId", "officeLocation", "status", "gender", "birthDate"
                )
                .targetType(EmployeeCsvDto.class)
                .build();
        if (lineCount > 0) {
            reader.setMaxItemCount(lineCount);
        }
        return reader;
    }

    // ------------------------------------------------------------------
    // Writer — thread-safe (stateless), shared across partitions
    // ------------------------------------------------------------------

    @Bean
    public JdbcBatchItemWriter<Employee> employeeItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Employee>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO employees (
                            employee_id, first_name, last_name, email, phone,
                            department, job_title, employment_type, hire_date, salary,
                            manager_id, office_location, status, gender, birth_date
                        ) VALUES (
                            :employeeId, :firstName, :lastName, :email, :phone,
                            :department, :jobTitle, :employmentType, :hireDate, :salary,
                            :managerId, :officeLocation, :status, :gender, :birthDate
                        )
                        """)
                .beanMapped()
                .build();
    }

    // ------------------------------------------------------------------
    // TaskExecutor — one virtual thread per partition
    // ------------------------------------------------------------------

    @Bean
    public TaskExecutor partitionTaskExecutor() {
        return new VirtualThreadTaskExecutor("batch-partition-");
    }

    // ------------------------------------------------------------------
    // Worker step — runs inside each partition
    // ------------------------------------------------------------------

    @Bean
    public Step employeeImportWorkerStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<EmployeeCsvDto> employeeItemReader,
            EmployeeItemProcessor employeeItemProcessor,
            JdbcBatchItemWriter<Employee> employeeItemWriter
    ) {
        return new StepBuilder("employeeImportWorkerStep", jobRepository)
                .<EmployeeCsvDto, Employee>chunk(CHUNK_SIZE, transactionManager)
                .reader(employeeItemReader)
                .processor(employeeItemProcessor)
                .writer(employeeItemWriter)
                .build();
    }

    // ------------------------------------------------------------------
    // Decompress step
    // ------------------------------------------------------------------

    @Bean
    @StepScope
    public DecompressTasklet decompressTasklet(
            @Value("#{jobParameters['filePath']}") String filePath
    ) {
        return new DecompressTasklet(filePath);
    }

    @Bean
    public Step decompressStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("decompressStep", jobRepository)
                .tasklet(decompressTasklet(null), transactionManager)
                .build();
    }

    // ------------------------------------------------------------------
    // Master step — fans out worker steps via partitioner
    // ------------------------------------------------------------------

    @Bean
    @StepScope
    public EmployeeCsvPartitioner employeeCsvPartitioner(
            @Value("#{jobExecutionContext['" + DecompressTasklet.TEMP_FILE_PATH_KEY + "']}") String tempFilePath
    ) {
        int totalLines = CsvFileUtils.countDataLines(tempFilePath);
        return new EmployeeCsvPartitioner(totalLines);
    }

    @Bean
    public Step employeeImportMasterStep(
            JobRepository jobRepository,
            Partitioner employeeCsvPartitioner,
            Step employeeImportWorkerStep
    ) {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(employeeImportWorkerStep);
        partitionHandler.setTaskExecutor(partitionTaskExecutor());
        partitionHandler.setGridSize(gridSize);

        return new StepBuilder("employeeImportMasterStep", jobRepository)
                .partitioner("employeeImportWorkerStep", employeeCsvPartitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    // ------------------------------------------------------------------
    // Cleanup step — runs after master step (success or failure)
    // ------------------------------------------------------------------

    @Bean
    public CleanupTasklet cleanupTasklet() {
        return new CleanupTasklet();
    }

    @Bean
    public Step cleanupStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("cleanupStep", jobRepository)
                .tasklet(cleanupTasklet(), transactionManager)
                .build();
    }

    // ------------------------------------------------------------------
    // Job — decompress → master → cleanup (cleanup always runs)
    // ------------------------------------------------------------------

    @Bean
    public Job employeeImportJob(
            JobRepository jobRepository,
            Step decompressStep,
            Step employeeImportMasterStep,
            Step cleanupStep
    ) {
        return new JobBuilder("employeeImportJob", jobRepository)
                .start(decompressStep)
                .next(employeeImportMasterStep)
                .on("*").to(cleanupStep)
                .end()
                .build();
    }

}
