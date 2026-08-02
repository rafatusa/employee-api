package com.example.employeeapi.api;

import com.example.employeeapi.exception.DuplicateEmailException;
import com.example.employeeapi.exception.EmployeeNotFoundException;
import com.example.employeeapi.model.Employee;
import com.example.employeeapi.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API-layer tests for EmployeeController.
 *
 * Uses @SpringBootTest (MOCK web env) + @AutoConfigureMockMvc so the real
 * SecurityConfig and JwtAuthFilter load correctly. EmployeeService is @MockBean
 * to control data outcomes. @WithMockUser / @WithAnonymousUser simulate auth.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Employee Controller API Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @Order(1)
    @WithMockUser
    @DisplayName("GET /api/employees returns empty list initially")
    void getAll_empty() throws Exception {
        when(employeeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(2)
    @WithMockUser
    @DisplayName("POST /api/employees creates a new employee")
    void create_success() throws Exception {
        Employee emp = new Employee("Jane", "Doe", "jane@example.com", "Engineering", 90000);
        Employee saved = new Employee("Jane", "Doe", "jane@example.com", "Engineering", 90000);
        saved.setId(1L);
        when(employeeService.create(any(Employee.class))).thenReturn(saved);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emp)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    @Order(3)
    @WithMockUser
    @DisplayName("POST /api/employees returns 400 for invalid payload")
    void create_invalidPayload() throws Exception {
        Employee invalid = new Employee("", "", "not-an-email", "", 0);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @WithMockUser
    @DisplayName("GET /api/employees/{id} returns employee by id")
    void getById_found() throws Exception {
        Employee emp = new Employee("Bob", "Smith", "bob@example.com", "HR", 70000);
        emp.setId(42L);
        when(employeeService.findById(42L)).thenReturn(emp);

        mockMvc.perform(get("/api/employees/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob@example.com"));
    }

    @Test
    @Order(5)
    @WithMockUser
    @DisplayName("GET /api/employees/{id} returns 404 for unknown id")
    void getById_notFound() throws Exception {
        when(employeeService.findById(99999L)).thenThrow(new EmployeeNotFoundException(99999L));

        mockMvc.perform(get("/api/employees/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("99999")));
    }

    @Test
    @Order(6)
    @WithMockUser
    @DisplayName("PUT /api/employees/{id} updates employee")
    void update_success() throws Exception {
        Employee updated = new Employee("Alice", "Wong", "alice.updated@example.com", "Finance", 92000);
        updated.setId(10L);
        when(employeeService.update(eq(10L), any(Employee.class))).thenReturn(updated);

        mockMvc.perform(put("/api/employees/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice.updated@example.com"))
                .andExpect(jsonPath("$.salary").value(92000.0));
    }

    @Test
    @Order(7)
    @WithMockUser
    @DisplayName("DELETE /api/employees/{id} removes employee")
    void delete_success() throws Exception {
        doNothing().when(employeeService).delete(5L);

        mockMvc.perform(delete("/api/employees/5"))
                .andExpect(status().isNoContent());

        verify(employeeService).delete(5L);
    }

    @Test
    @Order(8)
    @WithMockUser
    @DisplayName("POST /api/employees returns 409 on duplicate email")
    void create_duplicateEmail() throws Exception {
        Employee dup = new Employee("Another", "Person", "dup@example.com", "Ops", 60000);
        when(employeeService.create(any(Employee.class)))
                .thenThrow(new DuplicateEmailException("dup@example.com"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dup)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(9)
    @WithAnonymousUser
    @DisplayName("Unauthenticated request to /api/employees returns 401")
    void getAll_unauthorized() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }
}
