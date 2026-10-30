package com.manasvia.backend.modules.identity.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    @Test
    @DisplayName("isExpired() should return true for past dates and false for future dates")
    void testIsExpired() {
        RefreshToken validToken = RefreshToken.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();
        assertThat(validToken.isExpired()).isFalse();

        RefreshToken expiredToken = RefreshToken.builder()
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();
        assertThat(expiredToken.isExpired()).isTrue();
    }

    @Test
    @DisplayName("isValid() should return true only when not revoked AND not expired")
    void testIsValid() {
        RefreshToken validToken = RefreshToken.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        assertThat(validToken.isValid()).isTrue();

        RefreshToken revokedToken = RefreshToken.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .revoked(true)
                .build();
        assertThat(revokedToken.isValid()).isFalse();

        RefreshToken expiredToken = RefreshToken.builder()
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .revoked(false)
                .build();
        assertThat(expiredToken.isValid()).isFalse();
    }

    @Test
    @DisplayName("equals and hashCode contract based on entity ID")
    void testEqualsAndHashCode() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        RefreshToken token1 = RefreshToken.builder().id(id1).token("tok1").build();
        RefreshToken token2 = RefreshToken.builder().id(id1).token("tok2").build();
        RefreshToken token3 = RefreshToken.builder().id(id2).token("tok1").build();

        assertThat(token1).isEqualTo(token1);
        assertThat(token1).isEqualTo(token2);
        assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
        assertThat(token1).isNotEqualTo(token3);
        assertThat(token1).isNotEqualTo(null);
        assertThat(token1).isNotEqualTo("other-type");

        RefreshToken tokenNullId1 = new RefreshToken();
        RefreshToken tokenNullId2 = new RefreshToken();
        assertThat(tokenNullId1).isNotEqualTo(tokenNullId2);
    }
}
