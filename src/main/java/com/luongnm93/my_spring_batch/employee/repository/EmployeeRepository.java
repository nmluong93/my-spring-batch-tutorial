package com.luongnm93.my_spring_batch.employee.repository;

import com.luongnm93.my_spring_batch.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
}
