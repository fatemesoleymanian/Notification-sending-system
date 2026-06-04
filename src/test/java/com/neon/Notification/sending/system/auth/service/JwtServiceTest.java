package com.neon.Notification.sending.system.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "U2VjcmV0S2V5Rm9yTXlNaW5pRVJQU3lzdGVtMTIzIT8=",
            172_800_000L
    );

    @Test
    void generateTokenShouldRoundTripUsername() {
        String token = jwtService.generateToken("alice", Map.of("role", "ROLE_USER"));

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtService.isTokenValid(token, "alice")).isTrue();
    }

    @Test
    void tokenShouldBeInvalidForDifferentUsername() {
        String token = jwtService.generateToken("alice", Map.of("role", "ROLE_USER"));

        assertThat(jwtService.isTokenValid(token, "bob")).isFalse();
    }
}
