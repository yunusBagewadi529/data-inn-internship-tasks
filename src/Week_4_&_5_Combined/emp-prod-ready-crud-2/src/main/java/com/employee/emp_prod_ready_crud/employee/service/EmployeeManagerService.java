package com.employee.emp_prod_ready_crud.employee.service;

import com.employee.emp_prod_ready_crud.employee.entity.Employee;
import com.employee.emp_prod_ready_crud.employee.repository.EmployeeDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeManagerService {

    private final EmployeeDataRepository repository;

    // ----- CRUD -----

    public Employee createEmployee(Employee employee) {
        if (employee.getCreatedAt() == null) {
            employee.setCreatedAt(LocalDateTime.now());
        }
        // optionally set default status if null
        if (employee.getStatus() == null) {
            employee.setStatus("ACTIVE");
        }
        return repository.save(employee);
    }

    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    public Page<Employee> getAllEmployees(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return repository.findById(id);
    }

    public Optional<Employee> updateEmployee(Long id, Employee updated) {
        return repository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setEmail(updated.getEmail());
            existing.setDepartment(updated.getDepartment());
            existing.setSalary(updated.getSalary());
            existing.setStatus(updated.getStatus());
            existing.setUpdatedAt(LocalDateTime.now());
            return repository.save(existing);
        });
    }

    public void deleteEmployee(Long id) {
        repository.deleteById(id);
    }

    // queries with pagination

    public Page<Employee> getByDepartmentAndStatus(String department, String status, Pageable pageable) {
        return repository.findByDepartmentAndStatus(department, status, pageable);
    }

    public List<Employee> getByDepartmentAndStatus(String department, String status) {
        return repository.findByDepartmentAndStatus(department, status);
    }

    public Page<Employee> getBySalaryRange(Double min, Double max, Pageable pageable) {
        return repository.findBySalaryBetween(min, max, pageable);
    }

    public List<Employee> getBySalaryRange(Double min, Double max) {
        return repository.findBySalaryBetween(min, max);
    }

    public Page<Employee> getByNameAndDepartment(String name, String department, Pageable pageable) {
        return repository.findByNameContainingIgnoreCaseAndDepartment(name, department, pageable);
    }

    public List<Employee> getByNameAndDepartment(String name, String department) {
        return repository.findByNameContainingIgnoreCaseAndDepartment(name, department);
    }

    public Page<Employee> getByCreatedDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return repository.findByCreatedAtBetween(start, end, pageable);
    }

    public List<Employee> getByCreatedDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.findByCreatedAtBetween(start, end);
    }
}
