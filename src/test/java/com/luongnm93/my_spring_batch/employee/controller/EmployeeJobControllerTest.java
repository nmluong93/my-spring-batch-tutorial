package com.luongnm93.my_spring_batch.employee.controller;

import com.luongnm93.my_spring_batch.employee.service.GzFileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeJobController.class)
@org.springframework.test.context.TestPropertySource(properties = "batch.upload.directory=/tmp/test-job-source")
class EmployeeJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobLauncher jobLauncher;

    @MockitoBean
    private Job employeeImportJob;

    @MockitoBean
    private GzFileStorageService gzFileStorageService;

    @Test
    void importEmployees_returnsAcceptedWithExecutionId() throws Exception {
        when(gzFileStorageService.resolve(anyString()))
                .thenReturn(Path.of("/tmp/test-job-source/employees.csv"));

        JobExecution execution = mock(JobExecution.class);
        when(execution.getId()).thenReturn(42L);
        when(execution.getStatus()).thenReturn(org.springframework.batch.core.BatchStatus.STARTED);
        when(jobLauncher.run(any(), any())).thenReturn(execution);

        mockMvc.perform(post("/api/employees/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileName": "employees.csv"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobExecutionId").value(42));
    }

    @Test
    void importEmployees_returnsInternalServerErrorOnJobFailure() throws Exception {
        when(gzFileStorageService.resolve(anyString()))
                .thenReturn(Path.of("/tmp/test-job-source/employees.csv"));

        when(jobLauncher.run(any(), any()))
                .thenThrow(new RuntimeException("job launch failed"));

        mockMvc.perform(post("/api/employees/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileName": "employees.csv"}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("job launch failed"));
    }

    @Test
    void importEmployees_alreadyCompleteJob_returns409() throws Exception {
        when(gzFileStorageService.resolve(anyString()))
                .thenReturn(Path.of("/tmp/test-job-source/employees.csv"));

        when(jobLauncher.run(any(), any()))
                .thenThrow(new JobInstanceAlreadyCompleteException("already done"));

        mockMvc.perform(post("/api/employees/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileName": "employees.csv"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void importEmployees_blankFileName_returns400() throws Exception {
        mockMvc.perform(post("/api/employees/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileName": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadGzFile_validFile_returns200WithFileName() throws Exception {
        when(gzFileStorageService.store(any())).thenReturn("employees.csv.gz");

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "employees.csv.gz", "application/gzip", "gz-content".getBytes());

        mockMvc.perform(multipart("/api/employees/upload").file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("employees.csv.gz"));
    }

    @Test
    void uploadGzFile_invalidFile_returns400() throws Exception {
        when(gzFileStorageService.store(any()))
                .thenThrow(new IllegalArgumentException("Only .gz files are accepted"));

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "employees.csv", "text/csv", "content".getBytes());

        mockMvc.perform(multipart("/api/employees/upload").file(multipartFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only .gz files are accepted"));
    }

    @Test
    void uploadGzFile_ioException_returns500() throws Exception {
        when(gzFileStorageService.store(any()))
                .thenThrow(new IOException("disk full"));

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "employees.csv.gz", "application/gzip", "gz-content".getBytes());

        mockMvc.perform(multipart("/api/employees/upload").file(multipartFile))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("disk full"));
    }
}
