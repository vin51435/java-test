package com.manasvia.backend.modules.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload sent to refresh an expired JWT access token using a long-lived refresh token.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refresh token rotation request")
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken cannot be blank")
    @Schema(
            description = "Valid unrevoked refresh token issued during previous authentication",
            example = "4a18f7c9-9520-410a-b3e1-889410ea0921",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;
}
