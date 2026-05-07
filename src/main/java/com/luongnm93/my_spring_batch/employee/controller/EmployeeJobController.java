package com.luongnm93.my_spring_batch.employee.controller;

import com.luongnm93.my_spring_batch.employee.dto.EmployeeImportRequestDto;
import com.luongnm93.my_spring_batch.employee.service.GzFileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Jobs", description = "Endpoints for uploading employee data files and triggering the employee import batch job")
public class EmployeeJobController {

    private final JobLauncher jobLauncher;
    private final Job employeeImportJob;
    private final GzFileStorageService gzFileStorageService;

    @Operation(
            summary = "Upload a .gz employee file",
            description = "Accepts a gzip-compressed CSV file containing employee records and stores it for later batch processing."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File stored successfully; returns the stored file name",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"fileName\": \"employees_20260507.csv.gz\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid file (e.g. wrong format or empty)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"File must be a .gz archive\"}"))),
            @ApiResponse(responseCode = "500", description = "Internal error while storing the file",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"Unexpected IO error\"}")))
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadGzFile(
            @Parameter(name = "file", description = "Gzip-compressed CSV file containing employee records", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            String fileName = gzFileStorageService.store(file);
            return ResponseEntity.ok(Map.of("fileName", fileName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to store uploaded file", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Trigger the employee import batch job",
            description = "Resolves the previously uploaded file by name and launches the Spring Batch employee import job. Returns the job execution ID and initial status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Job accepted and launched",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"jobExecutionId\": 1, \"status\": \"STARTED\"}"))),
            @ApiResponse(responseCode = "409", description = "Job already completed for this file",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"Job already completed successfully for this file. No action taken.\"}"))),
            @ApiResponse(responseCode = "500", description = "Internal error while launching the job",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"error\": \"Unexpected error\"}")))
    })
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importEmployees(@Valid @org.springframework.web.bind.annotation.RequestBody EmployeeImportRequestDto request) {
        try {
            String fullPath = gzFileStorageService.resolve(request.getFileName()).toAbsolutePath().toString();

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("filePath", fullPath)
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(employeeImportJob, jobParameters);

            return ResponseEntity.accepted().body(Map.of(
                    "jobExecutionId", execution.getId(),
                    "status", execution.getStatus().name()
            ));
        } catch (JobInstanceAlreadyCompleteException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Job already completed successfully for this file. No action taken."
            ));
        } catch (Exception e) {
            log.error("Failed to launch employee import job", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
