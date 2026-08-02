package com.example.employeeapi.api;

import com.example.employeeapi.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API-layer tests for AuthController.
 *
 * Uses @SpringBootTest (MOCK web env) + @AutoConfigureMockMvc so the full
 * application context loads with the real SecurityConfig (circular dep resolved
 * via @Lazy). AuthenticationManager and JwtUtil are @MockBean so test outcomes
 * are deterministic without real credentials.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Controller API Tests")
class AuthControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authManager;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /api/auth/login returns JWT token for valid credentials")
    void login_success() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken("admin", null, List.of());
        when(authManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken("admin")).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 401 for invalid password")
    void login_invalidPassword() throws Exception {
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 401 for unknown user")
    void login_unknownUser() throws Exception {
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("unknown"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login returns 400 for missing username")
    void login_missingUsername() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"admin123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login returns 400 for missing password")
    void login_missingPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
