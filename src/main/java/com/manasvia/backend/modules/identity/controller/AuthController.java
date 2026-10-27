package com.manasvia.backend.modules.identity.controller;

import com.manasvia.backend.common.response.ApiResponse;
import com.manasvia.backend.modules.identity.dto.GuestAuthRequest;
import com.manasvia.backend.modules.identity.dto.TokenResponse;
import com.manasvia.backend.modules.identity.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST Controller handling anonymous guest authentication and session creation.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Initializes or resumes a frictionless anonymous guest session for mobile clients.
     *
     * @param request Contains client device fingerprint.
     * @return Standard {@link ApiResponse} wrapping the issued Bearer access token and refresh token.
     */
    @PostMapping("/guest")
    public ResponseEntity<ApiResponse<TokenResponse>> authenticateGuest(
            @Valid @RequestBody GuestAuthRequest request
    ) {
        TokenResponse tokenResponse = authService.authenticateGuest(request);
        return ResponseEntity.ok(ApiResponse.ok("Guest session initialized successfully", tokenResponse));
    }
}
