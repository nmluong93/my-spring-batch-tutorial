package com.luongnm93.my_spring_batch.employee.job;

import com.luongnm93.my_spring_batch.employee.dto.EmployeeCsvDto;
import com.luongnm93.my_spring_batch.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeItemProcessorTest {

    private EmployeeItemProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new EmployeeItemProcessor();
    }

    private EmployeeCsvDto buildDto() {
        EmployeeCsvDto dto = new EmployeeCsvDto();
        dto.setEmployeeId("EMP-000001");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setPhone("+1-555-0100");
        dto.setDepartment("Engineering");
        dto.setJobTitle("Software Engineer");
        dto.setEmploymentType("Full-Time");
        dto.setHireDate("2020-03-15");
        dto.setSalary("95000");
        dto.setManagerId(null);
        dto.setOfficeLocation("New York");
        dto.setStatus("Active");
        dto.setGender("Male");
        dto.setBirthDate("1990-07-22");
        return dto;
    }

    @Test
    void process_mapsAllFieldsCorrectly() throws Exception {
        Employee employee = processor.process(buildDto());

        assertThat(employee.getEmployeeId()).isEqualTo("EMP-000001");
        assertThat(employee.getFirstName()).isEqualTo("John");
        assertThat(employee.getLastName()).isEqualTo("Doe");
        assertThat(employee.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(employee.getPhone()).isEqualTo("+1-555-0100");
        assertThat(employee.getDepartment()).isEqualTo("Engineering");
        assertThat(employee.getJobTitle()).isEqualTo("Software Engineer");
        assertThat(employee.getEmploymentType()).isEqualTo("Full-Time");
        assertThat(employee.getHireDate()).isEqualTo(LocalDate.of(2020, 3, 15));
        assertThat(employee.getSalary()).isEqualTo(95000);
        assertThat(employee.getOfficeLocation()).isEqualTo("New York");
        assertThat(employee.getStatus()).isEqualTo("Active");
        assertThat(employee.getGender()).isEqualTo("Male");
        assertThat(employee.getBirthDate()).isEqualTo(LocalDate.of(1990, 7, 22));
    }

    @Test
    void process_nullManagerId_setsNull() throws Exception {
        EmployeeCsvDto dto = buildDto();
        dto.setManagerId(null);

        Employee employee = processor.process(dto);

        assertThat(employee.getManagerId()).isNull();
    }

    @Test
    void process_blankManagerId_setsNull() throws Exception {
        EmployeeCsvDto dto = buildDto();
        dto.setManagerId("   ");

        Employee employee = processor.process(dto);

        assertThat(employee.getManagerId()).isNull();
    }

    @Test
    void process_nonBlankManagerId_setsValue() throws Exception {
        EmployeeCsvDto dto = buildDto();
        dto.setManagerId("EMP-000002");

        Employee employee = processor.process(dto);

        assertThat(employee.getManagerId()).isEqualTo("EMP-000002");
    }

    @Test
    void process_parsesHireDateCorrectly() throws Exception {
        EmployeeCsvDto dto = buildDto();
        dto.setHireDate("2015-01-01");

        Employee employee = processor.process(dto);

        assertThat(employee.getHireDate()).isEqualTo(LocalDate.of(2015, 1, 1));
    }

    @Test
    void process_parsesBirthDateCorrectly() throws Exception {
        EmployeeCsvDto dto = buildDto();
        dto.setBirthDate("1985-12-31");

        Employee employee = processor.process(dto);

        assertThat(employee.getBirthDate()).isEqualTo(LocalDate.of(1985, 12, 31));
    }

    @Test
    void process_parsesSalaryToInteger() throws Exception {
        EmployeeCsvDto dto = buildDto();
        dto.setSalary("120000");

        Employee employee = processor.process(dto);

        assertThat(employee.getSalary()).isEqualTo(120000);
    }

    @Test
    void process_invalidHireDate_throwsException() {
        EmployeeCsvDto dto = buildDto();
        dto.setHireDate("not-a-date");

        assertThatThrownBy(() -> processor.process(dto))
                .isInstanceOf(Exception.class);
    }

    @Test
    void process_invalidSalary_throwsException() {
        EmployeeCsvDto dto = buildDto();
        dto.setSalary("not-a-number");

        assertThatThrownBy(() -> processor.process(dto))
                .isInstanceOf(NumberFormatException.class);
    }
}
