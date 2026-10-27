package com.manasvia.backend.common.security;

import com.manasvia.backend.modules.identity.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    // 256-bit test secret
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long ACCESS_EXPIRATION = 3600_000L; // 1 hour
    private static final long GUEST_EXPIRATION = 86400_000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, ACCESS_EXPIRATION, GUEST_EXPIRATION);
    }

    @Test
    @DisplayName("Should generate valid JWT access token and extract subject and role accurately")
    void testGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, UserRole.USER);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractRole(token)).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Should generate valid guest token with GUEST role")
    void testGenerateGuestToken() {
        UUID guestId = UUID.randomUUID();
        String guestToken = jwtService.generateGuestToken(guestId);

        assertThat(guestToken).isNotBlank();
        assertThat(jwtService.isTokenValid(guestToken)).isTrue();
        assertThat(jwtService.extractUserId(guestToken)).isEqualTo(guestId);
        assertThat(jwtService.extractRole(guestToken)).isEqualTo(UserRole.GUEST);
    }

    @Test
    @DisplayName("Should reject tampered or malformed token")
    void testInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalidpayload.invalidsignature";
        assertThat(jwtService.isTokenValid(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("Should identify expired token as invalid")
    void testExpiredToken() {
        UUID userId = UUID.randomUUID();
        // Generate with negative expiration (already expired in the past)
        String expiredToken = jwtService.generateToken(userId, UserRole.GUEST, java.util.Map.of(), -1000L);

        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
    }
}
