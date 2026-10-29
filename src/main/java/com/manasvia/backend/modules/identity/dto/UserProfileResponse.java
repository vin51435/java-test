package com.manasvia.backend.modules.identity.dto;

import com.manasvia.backend.common.security.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload returning full account details of the currently authenticated session.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed profile of the currently authenticated user session")
public class UserProfileResponse {

    @Schema(description = "Unique user identifier (UUID)", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID id;

    @Schema(description = "Assigned security role in the platform", example = "GUEST")
    private UserRole role;

    @Schema(description = "User display name", example = "Arjuna")
    private String displayName;

    @Schema(description = "Linked verified email address (populated if upgraded via Google or email)", example = "user@example.com")
    private String email;

    @Schema(description = "Email verification timestamp (null if unverified or guest)", example = "2026-10-28T10:00:00Z")
    private Instant emailVerifiedAt;

    @Schema(description = "Client device hardware identifier / fingerprint")
    private String deviceFingerprint;

    @Schema(description = "Account registration timestamp")
    private Instant createdAt;

    @Schema(description = "Whether the account is active")
    private boolean active;
}
