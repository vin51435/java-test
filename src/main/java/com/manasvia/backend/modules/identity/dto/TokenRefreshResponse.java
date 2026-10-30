package com.manasvia.backend.modules.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload returned after validating a refresh token and generating a new access token.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned after successful access token refresh")
public class TokenRefreshResponse {

    @Schema(description = "Freshly issued stateless HS256 JWT access token")
    private String accessToken;

    @Schema(description = "HTTP Authorization token scheme", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token time-to-live in seconds", example = "2592000")
    private long expiresIn;
}
