package com.luongnm93.my_spring_batch.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@Setter
public class Employee {

    @Id
    @Column(name = "employee_id", length = 20)
    private String employeeId;

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "phone", length = 30, nullable = false)
    private String phone;

    @Column(name = "department", length = 100, nullable = false)
    private String department;

    @Column(name = "job_title", length = 150, nullable = false)
    private String jobTitle;

    @Column(name = "employment_type", length = 20, nullable = false)
    private String employmentType;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "salary", nullable = false)
    private Integer salary;

    @Column(name = "manager_id", length = 20)
    private String managerId;

    @Column(name = "office_location", length = 150, nullable = false)
    private String officeLocation;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "gender", length = 10, nullable = false)
    private String gender;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
}
