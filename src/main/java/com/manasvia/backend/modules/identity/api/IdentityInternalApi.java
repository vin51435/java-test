package com.manasvia.backend.modules.identity.api;

import com.manasvia.backend.common.security.UserRole;

import java.util.Optional;
import java.util.UUID;

/**
 * Public programmatic Java contract exposed by the Identity module for inter-module collaboration.
 * Follows Spring Modulith interface segregation: other modules (Profile, Billing, Widget)
 * must interact with the Identity domain exclusively through this interface.
 */
public interface IdentityInternalApi {

    /**
     * Finds summary details of a user by their unique identifier.
     */
    Optional<UserSummaryDto> findUserById(UUID userId);

    /**
     * Checks if a user account exists by ID.
     */
    boolean existsById(UUID userId);

    /**
     * Upgrades or updates an account's security role (e.g. called by Billing when subscribing).
     */
    void upgradeUserRole(UUID userId, UserRole newRole);
}
