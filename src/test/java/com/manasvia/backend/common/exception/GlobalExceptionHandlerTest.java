package com.manasvia.backend.common.exception;

import com.manasvia.backend.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleIllegalArgumentException should return 400 with INVALID_ARGUMENT code")
    void testHandleIllegalArgument() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Invalid param")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError().code()).isEqualTo("INVALID_ARGUMENT");
    }

    @Test
    @DisplayName("handleAuthenticationFailedException should return 401 with domain error code")
    void testHandleAuthenticationFailed() {
        TokenExpiredException ex = new TokenExpiredException("Token has expired");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAuthenticationFailedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError().code()).isEqualTo("TOKEN_EXPIRED");
        assertThat(response.getBody().getError().message()).isEqualTo("Token has expired");
    }

    @Test
    @DisplayName("handleAuthenticationException should return 401 UNAUTHORIZED")
    void testHandleSpringAuthenticationException() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAuthenticationException(
                new BadCredentialsException("Bad credentials")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError().code()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    @DisplayName("handleAccessDeniedException should return 403 ACCESS_DENIED")
    void testHandleAccessDenied() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDeniedException(
                new AccessDeniedException("Access denied")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError().code()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    @DisplayName("handleNoResourceFoundException should return 404 NOT_FOUND")
    void testHandleNoResourceFound() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/unknown/path", "Resource not found");
        ResponseEntity<ApiResponse<Void>> response = handler.handleNoResourceFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError().code()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("handleGenericException should return 500 INTERNAL_SERVER_ERROR")
    void testHandleGenericException() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(
                new RuntimeException("Unexpected internal DB crash")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError().code()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getError().message()).isEqualTo("An unexpected error occurred. Please try again.");
    }
}
