package com.luongnm93.my_spring_batch;

import com.luongnm93.my_spring_batch.employee.controller.EmployeeJobController;
import com.luongnm93.my_spring_batch.employee.job.EmployeeImportJobConfig;
import com.luongnm93.my_spring_batch.employee.job.EmployeeItemProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MySpringBatchApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
    }

    @Test
    void employeeImportJobBeanIsRegistered() {
        Job job = context.getBean("employeeImportJob", Job.class);
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("employeeImportJob");
    }

    @Test
    void employeeItemProcessorBeanIsRegistered() {
        assertThat(context.getBean(EmployeeItemProcessor.class)).isNotNull();
    }

    @Test
    void employeeImportJobConfigBeanIsRegistered() {
        assertThat(context.getBean(EmployeeImportJobConfig.class)).isNotNull();
    }

    @Test
    void employeeImportControllerBeanIsRegistered() {
        assertThat(context.getBean(EmployeeJobController.class)).isNotNull();
    }
}
