package com.luongnm93.my_spring_batch.employee.controller;

import com.luongnm93.my_spring_batch.employee.service.GzFileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
}
