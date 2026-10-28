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
    @DisplayName("GET /api/v1/users/me - Should return 401 or 403 when no Bearer token provided")
    void testGetCurrentUserUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().is4xxClientError());
    }
}
