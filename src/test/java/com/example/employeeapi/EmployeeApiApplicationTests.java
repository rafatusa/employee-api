package com.example.employeeapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ImportAutoConfiguration(exclude = WebTestClientAutoConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.main.web-application-type=servlet")
@DisplayName("Application Context Load Test")
class EmployeeApiApplicationTests {

    @Test
    @DisplayName("Spring context loads successfully")
    void contextLoads() {
        // Verifies the whole application context starts without errors
    }
}
