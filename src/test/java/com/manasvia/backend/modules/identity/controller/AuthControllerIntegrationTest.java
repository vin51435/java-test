package com.manasvia.backend.modules.identity.controller;

import com.manasvia.backend.modules.identity.dto.GuestAuthRequest;
import com.manasvia.backend.modules.identity.dto.RefreshTokenRequest;
import com.manasvia.backend.modules.identity.model.RefreshToken;
import com.manasvia.backend.modules.identity.model.UserAccount;
import com.manasvia.backend.modules.identity.repository.RefreshTokenRepository;
import com.manasvia.backend.modules.identity.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("POST /api/v1/auth/guest - Should successfully register new guest and return tokens")
    void testAuthenticateNewGuest() throws Exception {
        String uniqueFingerprint = "device-fp-" + UUID.randomUUID();
        GuestAuthRequest request = new GuestAuthRequest(uniqueFingerprint);

        mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.role").value("GUEST"))
                .andExpect(jsonPath("$.data.userId").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/auth/guest - Idempotency: Multiple requests with same fingerprint return same userId")
    void testGuestAuthenticationIdempotency() throws Exception {
        String sharedFingerprint = "idempotent-device-" + UUID.randomUUID();
        GuestAuthRequest request = new GuestAuthRequest(sharedFingerprint);

        MvcResult firstCall = mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String firstUserId = objectMapper.readTree(firstCall.getResponse().getContentAsString())
                .path("data").path("userId").asText();

        MvcResult secondCall = mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String secondUserId = objectMapper.readTree(secondCall.getResponse().getContentAsString())
                .path("data").path("userId").asText();

        assertThat(firstUserId).isEqualTo(secondUserId);
    }

    @Test
    @DisplayName("POST /api/v1/auth/guest - Should return 400 when deviceFingerprint is blank")
    void testGuestAuthWithBlankFingerprint() throws Exception {
        GuestAuthRequest request = new GuestAuthRequest("");

        mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Security: Root endpoint should accept valid guest Bearer token")
    void testRootEndpointWithGuestToken() throws Exception {
        String uniqueFingerprint = "auth-filter-test-" + UUID.randomUUID();
        GuestAuthRequest request = new GuestAuthRequest(uniqueFingerprint);

        MvcResult authResult = mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(authResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // The root endpoint '/' is public and returns health info
        mockMvc.perform(get("/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should successfully issue new access token given valid refresh token")
    void testRefreshTokenSuccess() throws Exception {
        String uniqueFingerprint = "refresh-success-device-" + UUID.randomUUID();
        GuestAuthRequest guestRequest = new GuestAuthRequest(uniqueFingerprint);

        MvcResult guestResult = mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = objectMapper.readTree(guestResult.getResponse().getContentAsString())
                .path("data").path("refreshToken").asText();

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").isNumber());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should return 401 when refresh token does not exist")
    void testRefreshTokenInvalid() throws Exception {
        RefreshTokenRequest invalidRequest = new RefreshTokenRequest("non-existent-token-uuid");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should return 401 TOKEN_REVOKED when refresh token has been revoked")
    void testRefreshTokenRevoked() throws Exception {
        String uniqueFingerprint = "revoked-token-device-" + UUID.randomUUID();
        GuestAuthRequest guestRequest = new GuestAuthRequest(uniqueFingerprint);

        MvcResult guestResult = mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String rawToken = objectMapper.readTree(guestResult.getResponse().getContentAsString())
                .path("data").path("refreshToken").asText();

        // Mark the token as revoked in the database
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(rawToken).orElseThrow();
        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(rawToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TOKEN_REVOKED"))
                .andExpect(jsonPath("$.error.message").value("Refresh token has been revoked"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should return 401 TOKEN_EXPIRED when refresh token has expired")
    void testRefreshTokenExpired() throws Exception {
        String uniqueFingerprint = "expired-token-device-" + UUID.randomUUID();
        GuestAuthRequest guestRequest = new GuestAuthRequest(uniqueFingerprint);

        MvcResult guestResult = mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String rawToken = objectMapper.readTree(guestResult.getResponse().getContentAsString())
                .path("data").path("refreshToken").asText();

        // Set expiration into the past
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(rawToken).orElseThrow();
        tokenEntity.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
        refreshTokenRepository.save(tokenEntity);

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(rawToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TOKEN_EXPIRED"))
                .andExpect(jsonPath("$.error.message").value("Refresh token has expired"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should return 401 ACCOUNT_INACTIVE when user account is inactive")
    void testRefreshTokenUserInactive() throws Exception {
        String uniqueFingerprint = "inactive-user-device-" + UUID.randomUUID();
        GuestAuthRequest guestRequest = new GuestAuthRequest(uniqueFingerprint);

        MvcResult guestResult = mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String rawToken = objectMapper.readTree(guestResult.getResponse().getContentAsString())
                .path("data").path("refreshToken").asText();
        String userIdStr = objectMapper.readTree(guestResult.getResponse().getContentAsString())
                .path("data").path("userId").asText();

        // Deactivate the user
        UserAccount user = userRepository.findById(UUID.fromString(userIdStr)).orElseThrow();
        user.setActive(false);
        userRepository.save(user);

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(rawToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ACCOUNT_INACTIVE"))
                .andExpect(jsonPath("$.error.message").value("User account is inactive"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should return 400 VALIDATION_ERROR when refreshToken is blank")
    void testRefreshTokenBlank() throws Exception {
        RefreshTokenRequest invalidRequest = new RefreshTokenRequest("");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
