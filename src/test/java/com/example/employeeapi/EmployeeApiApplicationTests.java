package com.example.employeeapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Application Context Load Test")
class EmployeeApiApplicationTests {

    @Test
    @DisplayName("Spring context loads successfully")
    void contextLoads() {
        // Verifies the whole application context starts without errors
    }
}
