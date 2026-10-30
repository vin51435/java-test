package com.manasvia.backend.common.security;

import java.security.Principal;
import java.util.UUID;

/**
 * Lightweight immutable security principal stored in {@link org.springframework.security.core.context.SecurityContextHolder}.
 *
 * @param id   The authenticated user's unique identifier.
 * @param role The user's assigned role in the system.
 */
public record UserPrincipal(
        UUID id,
        UserRole role
) implements Principal {

    @Override
    public String getName() {
        return id != null ? id.toString() : "";
    }
}
