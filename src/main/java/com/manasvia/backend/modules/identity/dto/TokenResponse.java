package com.manasvia.backend.modules.identity.dto;

import com.manasvia.backend.common.security.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Standard security response payload returned upon successful authentication.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response payload containing stateless Bearer JWT and session tokens")
public class TokenResponse {

    @Schema(description = "Stateless HS256 JWT access token passed in 'Authorization: Bearer <token>'", example = "eyJhbGciOiJIUzI1NiIsIn...")
    private String accessToken;

    @Schema(description = "Long-lived cryptographically secure refresh token for session rotation", example = "4a18f7c9-9520-410a-b3e1-889410ea0921")
    private String refreshToken;

    @Schema(description = "HTTP Authorization token scheme", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token time-to-live in seconds", example = "2592000")
    private long expiresIn;

    @Schema(description = "Unique immutable identifier of the authenticated user account", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID userId;

    @Schema(description = "Assigned security role in the Manasvia platform", example = "GUEST")
    private UserRole role;
}
