package com.manasvia.backend.modules.identity.controller;

import com.manasvia.backend.modules.identity.dto.GuestAuthRequest;
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
}
