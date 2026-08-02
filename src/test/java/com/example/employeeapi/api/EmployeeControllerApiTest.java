package com.example.employeeapi.api;

import com.example.employeeapi.model.Employee;
import com.example.employeeapi.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ImportAutoConfiguration(exclude = WebTestClientAutoConfiguration.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.main.web-application-type=servlet")
@DisplayName("Employee Controller API Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    private static String jwtToken;

    @BeforeEach
    void cleanAndAuth() throws Exception {
        employeeRepository.deleteAll();

        if (jwtToken == null) {
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                    .andExpect(status().isOk())
                    .andReturn();
            String body = result.getResponse().getContentAsString();
            jwtToken = objectMapper.readTree(body).get("token").asText();
        }
    }

    @Test
    @Order(1)
    @DisplayName("GET /api/employees returns empty list initially")
    void getAll_empty() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/employees creates a new employee")
    void create_success() throws Exception {
        Employee emp = new Employee("Jane", "Doe", "jane@example.com", "Engineering", 90000);
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emp)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/employees returns 400 for invalid payload")
    void create_invalidPayload() throws Exception {
        Employee invalid = new Employee("", "", "not-an-email", "", 0);
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/employees/{id} returns employee by id")
    void getById_found() throws Exception {
        Employee saved = employeeRepository.save(
                new Employee("Bob", "Smith", "bob@example.com", "HR", 70000));
        mockMvc.perform(get("/api/employees/" + saved.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob@example.com"));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/employees/{id} returns 404 for unknown id")
    void getById_notFound() throws Exception {
        mockMvc.perform(get("/api/employees/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("99999")));
    }

    @Test
    @Order(6)
    @DisplayName("PUT /api/employees/{id} updates employee")
    void update_success() throws Exception {
        Employee saved = employeeRepository.save(
                new Employee("Alice", "Wong", "alice@example.com", "Finance", 85000));
        Employee updated = new Employee("Alice", "Wong", "alice.updated@example.com", "Finance", 92000);
        mockMvc.perform(put("/api/employees/" + saved.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice.updated@example.com"))
                .andExpect(jsonPath("$.salary").value(92000.0));
    }

    @Test
    @Order(7)
    @DisplayName("DELETE /api/employees/{id} removes employee")
    void delete_success() throws Exception {
        Employee saved = employeeRepository.save(
                new Employee("Tom", "Harris", "tom@example.com", "IT", 75000));
        mockMvc.perform(delete("/api/employees/" + saved.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/employees/" + saved.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/employees returns 409 on duplicate email")
    void create_duplicateEmail() throws Exception {
        Employee emp = new Employee("Dup", "User", "dup@example.com", "Ops", 60000);
        employeeRepository.save(emp);
        Employee dup = new Employee("Another", "Person", "dup@example.com", "Ops", 60000);
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dup)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(9)
    @DisplayName("Unauthenticated request to /api/employees returns 401")
    void getAll_unauthorized() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }
}
