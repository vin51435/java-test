package com.manasvia.backend.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class RestSecurityHandlersTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("RestAuthenticationEntryPoint should write 401 JSON ApiResponse")
    void testAuthenticationEntryPoint() throws Exception {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(objectMapper);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Bad credentials"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).contains("application/json");

        String body = response.getContentAsString();
        assertThat(body).contains("\"success\":false");
        assertThat(body).contains("\"code\":\"UNAUTHORIZED\"");
        assertThat(body).contains("Full authentication is required to access this resource");
    }

    @Test
    @DisplayName("RestAccessDeniedHandler should write 403 JSON ApiResponse")
    void testAccessDeniedHandler() throws Exception {
        RestAccessDeniedHandler deniedHandler = new RestAccessDeniedHandler(objectMapper);

        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/v1/admin/resource");
        MockHttpServletResponse response = new MockHttpServletResponse();

        deniedHandler.handle(request, response, new AccessDeniedException("Access is denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).contains("application/json");

        String body = response.getContentAsString();
        assertThat(body).contains("\"success\":false");
        assertThat(body).contains("\"code\":\"FORBIDDEN\"");
        assertThat(body).contains("Access denied: insufficient permissions");
    }
}
