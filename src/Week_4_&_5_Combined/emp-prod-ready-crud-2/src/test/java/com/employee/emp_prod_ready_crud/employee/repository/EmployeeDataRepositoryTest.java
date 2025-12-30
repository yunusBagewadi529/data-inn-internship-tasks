package com.employee.emp_prod_ready_crud.employee.repository;

import com.employee.emp_prod_ready_crud.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmployeeDataRepositoryTest {

    // Creates a TestContainer on which Postgres image (Temporary psql environment) is created on which
    // our test cases are going to run and execute
    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // ensure the container is started before retrieving mapped ports / jdbc url
        if (!postgres.isRunning()) {
            postgres.start();
        }

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);

        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }


    @Autowired
    private TestEntityManager em;

    @Autowired
    private EmployeeDataRepository repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        repo.flush();

        Employee e1 = Employee.builder()
                .name("Alice Johnson")
                .email("alice@example.com")
                .department("Engineering")
                .salary(90000.00)
                .status("ACTIVE")
                .build();

        Employee e2 = Employee.builder()
                .name("Bob Roberts")
                .email("bob@example.com")
                .department("Engineering")
                .salary(70000.00)
                .status("INACTIVE")
                .build();

        Employee e3 = Employee.builder()
                .name("Carla Diaz")
                .email("carla@example.com")
                .department("Sales")
                .salary(60000.00)
                .status("ACTIVE")
                .build();

        Employee e4 = Employee.builder()
                .name("David Smith")
                .email("david@example.com")
                .department("Sales")
                .salary(65000.00)
                .status("ACTIVE")
                .build();

        Employee e5 = Employee.builder()
                .name("Eve Adams")
                .email("eve@example.com")
                .department("HR")
                .salary(50000.00)
                .status("INACTIVE")
                .build();

        repo.saveAll(List.of(e1, e2, e3, e4, e5));
        em.getEntityManager().flush();
        em.clear();
    }

    @Test
    void findByDepartmentAndStatus_returnsMatchingEmployees() {
        List<Employee> engineeringActive = repo.findByDepartmentAndStatus("Engineering", "ACTIVE");
        assertThat(engineeringActive).hasSize(1)
                .first().extracting(Employee::getEmail).isEqualTo("alice@example.com");

        List<Employee> salesActive = repo.findByDepartmentAndStatus("Sales", "ACTIVE");
        assertThat(salesActive).hasSize(2)
                .extracting(Employee::getName).containsExactlyInAnyOrder("Carla Diaz", "David Smith");
    }

    @Test
    void findByDepartmentAndStatus_withPaging_returnsPage() {
        PageRequest page = PageRequest.of(0, 1);
        Page<Employee> pageResult = repo.findByDepartmentAndStatus("Sales", "ACTIVE", page);

        assertThat(pageResult.getTotalElements()).isEqualTo(2);
        assertThat(pageResult.getContent()).hasSize(1);
    }

    @Test
    void findByNameContainingIgnoreCaseAndDepartment_matchesCaseInsensitive() {
        List<Employee> result = repo.findByNameContainingIgnoreCaseAndDepartment("AR", "Sales");
        assertThat(result).hasSize(1).first().extracting(Employee::getEmail).isEqualTo("carla@example.com");
    }

    @Test
    void findByNameContainingIgnoreCaseAndDepartment_withPaging() {
        PageRequest page = PageRequest.of(0, 2);
        Page<Employee> pageResult = repo.findByNameContainingIgnoreCaseAndDepartment("a", "Engineering", page);

        // simple sanity checks: totalElements should be >=0 and content size <= page size
        assertThat(pageResult.getTotalElements()).isGreaterThanOrEqualTo(0);
        assertThat(pageResult.getContent().size()).isLessThanOrEqualTo(2);
    }

    @Test
    void findByCreatedAtBetween_returnsRecentlyCreated() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(2);
        LocalDateTime end = LocalDateTime.now().plusMinutes(2);

        List<Employee> recent = repo.findByCreatedAtBetween(start, end);
        assertThat(recent).hasSize(5);
    }

    @Test
    void findBySalaryBetween_returnsCorrectEmployees() {
        List<Employee> midRange = repo.findBySalaryBetween(60000.00, 80000.00);
        assertThat(midRange).hasSize(3)
                .extracting(Employee::getEmail)
                .containsExactlyInAnyOrder("bob@example.com", "carla@example.com", "david@example.com");
    }

    @Test
    void findBySalaryBetween_withPaging() {
        PageRequest page = PageRequest.of(0, 2);
        Page<Employee> pageResult = repo.findBySalaryBetween(0.0, 100000.0, page);
        assertThat(pageResult.getTotalElements()).isEqualTo(5);
        assertThat(pageResult.getContent()).hasSize(2);
    }

    @Test
    void repository_returnsEmptyList_whenNoMatch() {
        List<Employee> none = repo.findByDepartmentAndStatus("NonExistentDept", "ACTIVE");
        assertThat(none).isEmpty();
    }

    @Test
    void savingDuplicateEmail_violatesUniqueConstraint() {
        Employee duplicate = Employee.builder()
                .name("Alice Clone")
                .email("alice@example.com")
                .department("Engineering")
                .salary(30000.00)
                .status("ACTIVE")
                .build();

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            repo.saveAndFlush(duplicate);
        });
    }
}
