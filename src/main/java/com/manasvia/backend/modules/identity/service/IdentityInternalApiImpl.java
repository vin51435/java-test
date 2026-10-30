package com.manasvia.backend.modules.identity.service;

import com.manasvia.backend.modules.identity.api.IdentityInternalApi;
import com.manasvia.backend.modules.identity.api.UserSummaryDto;
import com.manasvia.backend.common.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link IdentityInternalApi} serving inter-module requests.
 * Delegates account queries and modifications to {@link UserService}.
 */
@Service
@RequiredArgsConstructor
public class IdentityInternalApiImpl implements IdentityInternalApi {

    private final UserService userService;

    @Override
    public Optional<UserSummaryDto> findUserById(UUID userId) {
        return userService.findById(userId)
                .map(u -> new UserSummaryDto(u.getId(), u.getRole(), u.isActive(), u.getCreatedAt()));
    }

    @Override
    public boolean existsById(UUID userId) {
        return userService.existsById(userId);
    }

    @Override
    public void upgradeUserRole(UUID userId, UserRole newRole) {
        userService.upgradeUserRole(userId, newRole);
    }
}
