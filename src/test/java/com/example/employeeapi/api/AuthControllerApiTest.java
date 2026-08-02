package com.example.employeeapi.api;

import com.example.employeeapi.config.TestSecurityConfig;
import com.example.employeeapi.controller.AuthController;
import com.example.employeeapi.exception.GlobalExceptionHandler;
import com.example.employeeapi.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API-layer tests for AuthController using @WebMvcTest.
 *
 * Uses TestSecurityConfig (no JWT filter, no circular deps) to mirror production
 * access rules. AuthenticationManager and JwtUtil are mocked to control outcomes.
 */
@WebMvcTest(controllers = AuthController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
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
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("unknown user"));

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
