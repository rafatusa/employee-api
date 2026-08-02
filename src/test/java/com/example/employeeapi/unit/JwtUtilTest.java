package com.example.employeeapi.unit;

import com.example.employeeapi.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // 32+ char secret required for HS256
        jwtUtil = new JwtUtil("test-secret-key-that-is-long-enough-for-hmac", 3600000L);
    }

    @Test
    @DisplayName("generateToken returns a non-null token string")
    void generateToken_notNull() {
        String token = jwtUtil.generateToken("admin");
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extractUsername returns the correct subject")
    void extractUsername_correct() {
        String token = jwtUtil.generateToken("admin");
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("isTokenValid returns true for a fresh token")
    void isTokenValid_validToken() {
        String token = jwtUtil.generateToken("admin");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid returns false for a tampered token")
    void isTokenValid_tamperedToken() {
        String token = jwtUtil.generateToken("admin");
        String tampered = token.substring(0, token.length() - 4) + "xxxx";
        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for a blank string")
    void isTokenValid_blankToken() {
        assertThat(jwtUtil.isTokenValid("")).isFalse();
    }

    @Test
    @DisplayName("expired token is invalid")
    void isTokenValid_expiredToken() {
        JwtUtil shortLived = new JwtUtil("test-secret-key-that-is-long-enough-for-hmac", 1L);
        String token = shortLived.generateToken("admin");
        // Token with 1ms TTL — sleep to guarantee expiry
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        assertThat(shortLived.isTokenValid(token)).isFalse();
    }
}
