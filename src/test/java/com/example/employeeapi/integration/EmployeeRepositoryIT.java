package com.example.employeeapi.integration;

import com.example.employeeapi.model.Employee;
import com.example.employeeapi.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("EmployeeRepository Integration Tests")
class EmployeeRepositoryIT {

    @Autowired
    private EmployeeRepository repository;

    @BeforeEach
    void clear() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("save and findById persists an employee")
    void saveAndFind() {
        Employee emp = new Employee("Jane", "Doe", "jane@it.com", "IT", 85000);
        Employee saved = repository.save(emp);
        Optional<Employee> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("jane@it.com");
    }

    @Test
    @DisplayName("findAll returns all saved employees")
    void findAll() {
        repository.save(new Employee("A", "B", "a@b.com", "Dept", 50000));
        repository.save(new Employee("C", "D", "c@d.com", "Dept", 60000));
        List<Employee> all = repository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("findByEmail returns employee by email")
    void findByEmail() {
        repository.save(new Employee("X", "Y", "x@y.com", "Sales", 70000));
        Optional<Employee> result = repository.findByEmail("x@y.com");
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("X");
    }

    @Test
    @DisplayName("existsByEmail returns true for existing email")
    void existsByEmail_true() {
        repository.save(new Employee("E", "F", "e@f.com", "Ops", 65000));
        assertThat(repository.existsByEmail("e@f.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail returns false for absent email")
    void existsByEmail_false() {
        assertThat(repository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    @DisplayName("deleteById removes the employee")
    void deleteById() {
        Employee saved = repository.save(new Employee("Del", "Me", "del@me.com", "Temp", 40000));
        repository.deleteById(saved.getId());
        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
