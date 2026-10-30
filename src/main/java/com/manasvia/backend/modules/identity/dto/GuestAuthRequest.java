package com.manasvia.backend.modules.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload sent by mobile client during anonymous guest session initialization.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Anonymous guest authentication payload containing client device fingerprint")
public class GuestAuthRequest {

    @Schema(
            description = "Unique client-side device identifier / hardware fingerprint",
            example = "android-glance-9f82c47a1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "deviceFingerprint cannot be blank")
    @Size(min = 8, max = 255, message = "deviceFingerprint must be between 8 and 255 characters")
    private String deviceFingerprint;
}
