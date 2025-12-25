package com.employee.emp_prod_ready_crud.employee.controller;

import com.employee.emp_prod_ready_crud.employee.entity.Employee;
import com.employee.emp_prod_ready_crud.employee.service.EmployeeManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeApiController {

    private final EmployeeManagerService service;

    // Generic paginated list
    // GET /api/employees?page=0&size=10&sortBy=id&direction=ASC
    @GetMapping
    public ResponseEntity<Page<Employee>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        Page<Employee> employees = service.getAllEmployees(pageable);
        return ResponseEntity.ok(employees);
    }

    // Create
    @PostMapping("/create")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee created = service.createEmployee(employee);

        URI location = URI.create(String.format("/api/employees/%d", created.getId()));
        return ResponseEntity.created(location).body(created);
    }

    // Read all (non-paginated)
    @GetMapping("/get-all")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(service.getAllEmployees());
    }

    // Read by id

    @GetMapping("/get/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Optional<Employee> opt = service.getEmployeeById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    // Update
    @PutMapping("/update/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @RequestBody Employee employee
    ) {
        Optional<Employee> updated = service.updateEmployee(id, employee);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    // Delete
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Employee> deleteEmployee(@PathVariable Long id) {
        Optional<Employee> employeeOpt = service.getEmployeeById(id);

        if (employeeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        service.deleteEmployee(id);
        return ResponseEntity.ok(employeeOpt.get());
    }


    // Search endpoints — paginated (support page,size,sortBy,direction)
    @GetMapping("/search/by-department-status")
    public ResponseEntity<Page<Employee>> getByDepartmentAndStatus(
            @RequestParam String department,
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Page<Employee> result = service.getByDepartmentAndStatus(department, status, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-salary-range")
    public ResponseEntity<Page<Employee>> getBySalaryRange(
            @RequestParam Double min,
            @RequestParam Double max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "salary") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Page<Employee> result = service.getBySalaryRange(min, max, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-name-department")
    public ResponseEntity<Page<Employee>> getByNameAndDepartment(
            @RequestParam String name,
            @RequestParam String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Page<Employee> result = service.getByNameAndDepartment(name, department, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-created-date")
    public ResponseEntity<Page<Employee>> getByCreatedDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Page<Employee> result = service.getByCreatedDateRange(start, end, pageable);
        return ResponseEntity.ok(result);
    }
}
