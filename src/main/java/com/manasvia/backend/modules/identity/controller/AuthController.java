package com.manasvia.backend.modules.identity.controller;

import com.manasvia.backend.common.response.ApiResponse;
import com.manasvia.backend.modules.identity.dto.GuestAuthRequest;
import com.manasvia.backend.modules.identity.dto.RefreshTokenRequest;
import com.manasvia.backend.modules.identity.dto.TokenRefreshResponse;
import com.manasvia.backend.modules.identity.dto.TokenResponse;
import com.manasvia.backend.modules.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST Controller handling anonymous guest authentication, session creation, and token rotation.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "01. Authentication & Sessions", description = "Frictionless guest onboarding, token rotation, and credential exchange")
public class AuthController {

    private final AuthService authService;

    /**
     * Initializes or resumes a frictionless anonymous guest session for mobile clients.
     *
     * @param request Contains client device fingerprint.
     * @return Standard {@link ApiResponse} wrapping the issued Bearer access token and refresh token.
     */
    @PostMapping("/guest")
    @Operation(
            summary = "Initialize or resume anonymous guest session",
            description = "Idempotently resolves or registers a guest account using the provided device fingerprint. " +
                    "Returns a 30-day extended HS256 JWT access token and a 60-day refresh token."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Guest authenticated successfully. Returns valid access and refresh tokens."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload or blank device fingerprint."
            )
    })
    public ResponseEntity<ApiResponse<TokenResponse>> authenticateGuest(
            @Valid @RequestBody GuestAuthRequest request
    ) {
        TokenResponse tokenResponse = authService.authenticateGuest(request);
        return ResponseEntity.ok(ApiResponse.ok("Guest session initialized successfully", tokenResponse));
    }

    /**
     * Issues a fresh stateless access token using a valid, unrevoked refresh token.
     *
     * @param request Contains the long-lived refresh token.
     * @return Standard {@link ApiResponse} wrapping the new access token.
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Rotate access token using refresh token",
            description = "Validates the refresh token against the database and issues a fresh JWT access token without requiring re-login."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Access token successfully refreshed."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid or blank refresh token."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh token expired or revoked."
            )
    })
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        TokenRefreshResponse response = authService.refreshAccessToken(request);
        return ResponseEntity.ok(ApiResponse.ok("Access token refreshed successfully", response));
    }
}
