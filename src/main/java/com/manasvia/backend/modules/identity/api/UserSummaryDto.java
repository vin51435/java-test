package com.manasvia.backend.modules.identity.api;

import com.manasvia.backend.common.security.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Public immutable DTO representing an account summary.
 * Safe to be consumed by other bounded context modules (e.g. Profile, Billing).
 */
public record UserSummaryDto(
        UUID id,
        UserRole role,
        boolean active,
        Instant createdAt
) {}
