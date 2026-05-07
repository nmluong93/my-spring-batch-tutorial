package com.luongnm93.my_spring_batch.employee.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeCsvDto {

    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private String jobTitle;
    private String employmentType;
    private String hireDate;
    private String salary;
    private String managerId;
    private String officeLocation;
    private String status;
    private String gender;
    private String birthDate;
}
