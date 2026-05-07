package com.luongnm93.my_spring_batch.employee.job;

import com.luongnm93.my_spring_batch.employee.dto.EmployeeCsvDto;
import com.luongnm93.my_spring_batch.employee.entity.Employee;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Component
public class EmployeeItemProcessor implements ItemProcessor<EmployeeCsvDto, Employee> {

    @Override
    public Employee process(EmployeeCsvDto dto) {
        Employee employee = new Employee();
        employee.setEmployeeId(dto.getEmployeeId());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDepartment(dto.getDepartment());
        employee.setJobTitle(dto.getJobTitle());
        employee.setEmploymentType(dto.getEmploymentType());
        employee.setHireDate(LocalDate.parse(dto.getHireDate()));
        employee.setSalary(Integer.parseInt(dto.getSalary()));
        employee.setManagerId(dto.getManagerId() == null || dto.getManagerId().isBlank() ? null : dto.getManagerId());
        employee.setOfficeLocation(dto.getOfficeLocation());
        employee.setStatus(dto.getStatus());
        employee.setGender(dto.getGender());
        employee.setBirthDate(LocalDate.parse(dto.getBirthDate()));
        return employee;
    }
}
