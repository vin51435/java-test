package com.manasvia.backend.modules.identity.controller;

import com.manasvia.backend.modules.identity.dto.GuestAuthRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.manasvia.backend.common.security.JwtService jwtService;

    @Test
    @DisplayName("GET /api/v1/users/me - Should return 200 with user profile when valid Bearer token provided")
    void testGetCurrentUserWithBearerToken() throws Exception {
        String uniqueFingerprint = "user-ctrl-me-" + UUID.randomUUID();
        GuestAuthRequest guestRequest = new GuestAuthRequest(uniqueFingerprint);

        MvcResult guestResult = mockMvc.perform(post("/api/v1/auth/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(guestResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
        String userId = objectMapper.readTree(guestResult.getResponse().getContentAsString())
                .path("data").path("userId").asText();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.role").value("GUEST"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.deviceFingerprint").value(uniqueFingerprint));
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Should return 401 JSON ApiResponse when no Bearer token provided")
    void testGetCurrentUserUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.error.message").value("Full authentication is required to access this resource"));
    }

    @Test
    @DisplayName("GET /api/v1/ - Unauthenticated request should return 401 JSON ApiResponse instead of HTML Whitelabel")
    void testUnauthenticatedApiRootReturnsJson() throws Exception {
        mockMvc.perform(get("/api/v1/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Should return 401 when invalid Bearer token is provided")
    void testGetCurrentUserWithInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer invalid.jwt.signature"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Should return 401 when non-Bearer Authorization header is provided")
    void testGetCurrentUserWithNonBearerHeader() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Should return 400 INVALID_ARGUMENT when valid JWT belongs to nonexistent user")
    void testGetCurrentUserForDeletedUser() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
        String validJwtForDeletedUser = jwtService.generateGuestToken(nonExistentUserId);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + validJwtForDeletedUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"));
    }
}
