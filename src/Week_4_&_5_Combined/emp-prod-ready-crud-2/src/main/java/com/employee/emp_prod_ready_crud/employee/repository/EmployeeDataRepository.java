package com.employee.emp_prod_ready_crud.employee.repository;

import com.employee.emp_prod_ready_crud.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EmployeeDataRepository extends JpaRepository<Employee, Long> {

    // Department + Status
    List<Employee> findByDepartmentAndStatus(String department, String status);
    Page<Employee> findByDepartmentAndStatus(String department, String status, Pageable pageable);

    // Name contains + Department
    List<Employee> findByNameContainingIgnoreCaseAndDepartment(String name, String department);
    Page<Employee> findByNameContainingIgnoreCaseAndDepartment(String name, String department, Pageable pageable);

    // Created date range
    List<Employee> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Employee> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Salary range
    List<Employee> findBySalaryBetween(Double min, Double max);
    Page<Employee> findBySalaryBetween(Double min, Double max, Pageable pageable);
}
