package com.example.employeeapi.unit;

import com.example.employeeapi.exception.DuplicateEmailException;
import com.example.employeeapi.exception.EmployeeNotFoundException;
import com.example.employeeapi.model.Employee;
import com.example.employeeapi.repository.EmployeeRepository;
import com.example.employeeapi.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Unit Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeService service;

    private Employee emp;

    @BeforeEach
    void setUp() {
        emp = new Employee("Jane", "Doe", "jane@example.com", "Engineering", 90000);
        emp.setId(1L);
    }

    @Test
    @DisplayName("findAll returns all employees")
    void findAll_returnsAll() {
        when(repository.findAll()).thenReturn(List.of(emp));
        List<Employee> result = service.findAll();
        assertThat(result).hasSize(1).contains(emp);
    }

    @Test
    @DisplayName("findById returns employee when found")
    void findById_found() {
        when(repository.findById(1L)).thenReturn(Optional.of(emp));
        Employee result = service.findById(1L);
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("findById throws EmployeeNotFoundException when not found")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create saves and returns employee")
    void create_success() {
        when(repository.existsByEmail(emp.getEmail())).thenReturn(false);
        when(repository.save(any(Employee.class))).thenReturn(emp);
        Employee result = service.create(emp);
        assertThat(result.getFirstName()).isEqualTo("Jane");
        verify(repository).save(emp);
    }

    @Test
    @DisplayName("create throws DuplicateEmailException on duplicate email")
    void create_duplicateEmail() {
        when(repository.existsByEmail(emp.getEmail())).thenReturn(true);
        assertThatThrownBy(() -> service.create(emp))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("jane@example.com");
    }

    @Test
    @DisplayName("update modifies employee fields")
    void update_success() {
        Employee updated = new Employee("John", "Doe", "john@example.com", "HR", 80000);
        when(repository.findById(1L)).thenReturn(Optional.of(emp));
        when(repository.existsByEmail("john@example.com")).thenReturn(false);
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        Employee result = service.update(1L, updated);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getDepartment()).isEqualTo("HR");
    }

    @Test
    @DisplayName("delete removes employee by id")
    void delete_success() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);
        assertThatCode(() -> service.delete(1L)).doesNotThrowAnyException();
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("delete throws EmployeeNotFoundException when not found")
    void delete_notFound() {
        when(repository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(EmployeeNotFoundException.class);
    }
}
