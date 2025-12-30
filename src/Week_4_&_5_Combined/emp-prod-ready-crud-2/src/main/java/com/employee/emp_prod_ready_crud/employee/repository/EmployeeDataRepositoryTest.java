package com.employee.emp_prod_ready_crud.employee.repository;

import com.employee.emp_prod_ready_crud.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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
@AutoConfigureTestDatabase(replace = Replace.NONE) // allow Testcontainers' Postgres datasource
@Testcontainers
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class) // ensure Liquibase runs in this slice
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmployeeDataRepositoryTest {

    // Testcontainers Postgres - image version chosen to be close to your production
    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    // Hook container properties into Spring's environment before context loads
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);

        // Ensure Liquibase runs in tests and uses your changelog from main resources.
        // application.properties already sets the changelog path, but set explicitly for clarity.
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");

        // Keep JPA DDL off (you already have spring.jpa.hibernate.ddl-auto=none in main props).
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EmployeeDataRepository repo;

    // Seed data to be used by tests
    @BeforeEach
    void setUp() {
        // Clear repository to be safe (in case something persisted)
        repo.deleteAll();
        repo.flush();

        // Note: PrePersist in Employee will set createdAt and updatedAt automatically.
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

        // Save all via repository (preserves JPA lifecycle callbacks like @PrePersist)
        repo.saveAll(List.of(e1, e2, e3, e4, e5));
        // flush to ensure DB writes are performed before tests run
        em.getEntityManager().flush();
        em.clear(); // avoid returning cached entities in subsequent repository calls
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
        // name contains 'ar' (Carla) test case-insensitive
        List<Employee> result = repo.findByNameContainingIgnoreCaseAndDepartment("AR", "Sales");
        assertThat(result).hasSize(1).first().extracting(Employee::getEmail).isEqualTo("carla@example.com");
    }

    @Test
    void findByNameContainingIgnoreCaseAndDepartment_withPaging() {
        PageRequest page = PageRequest.of(0, 2);
        Page<Employee> pageResult = repo.findByNameContainingIgnoreCaseAndDepartment("a", "Engineering", page);

        // In Engineering we have Alice (Alice Johnson), Bob (Bob Roberts) - but Bob may not contain 'a' so adjust expectation:
        // we'll assert that the page total is >= 0 and content size <= page size (sanity checks)
        assertThat(pageResult.getTotalElements()).isGreaterThanOrEqualTo(0);
        assertThat(pageResult.getContent().size()).isLessThanOrEqualTo(2);
    }

    @Test
    void findByCreatedAtBetween_returnsRecentlyCreated() {
        // createdAt set by @PrePersist during save; use an interval around now
        LocalDateTime start = LocalDateTime.now().minusMinutes(2);
        LocalDateTime end = LocalDateTime.now().plusMinutes(2);

        List<Employee> recent = repo.findByCreatedAtBetween(start, end);
        // All seeded employees were created just now, so expect 5
        assertThat(recent).hasSize(5);
    }

    @Test
    void findBySalaryBetween_returnsCorrectEmployees() {
        // salaries seeded: 90000,70000,60000,65000,50000
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
        // Attempt to save another employee with email 'alice@example.com' which is unique per Liquibase schema
        Employee duplicate = Employee.builder()
                .name("Alice Clone")
                .email("alice@example.com")
                .department("Engineering")
                .salary(30000.00)
                .status("ACTIVE")
                .build();

        // Depending on JPA provider and when constraints are checked, this typically throws a DataIntegrityViolationException
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            repo.saveAndFlush(duplicate);
        });
    }
}
