package com.manasvia.backend.modules.identity.service;

import com.manasvia.backend.modules.identity.dto.UserProfileResponse;
import com.manasvia.backend.modules.identity.model.UserAccount;
import com.manasvia.backend.common.security.UserRole;
import com.manasvia.backend.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service managing user account state, queries, and profile lifecycle.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    /**
     * Retrieves account profile details for the authenticated user session.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return UserProfileResponse.builder()
                .id(user.getId())
                .role(user.getRole())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .deviceFingerprint(user.getDeviceFingerprint())
                .createdAt(user.getCreatedAt())
                .active(user.isActive())
                .build();
    }

    /**
     * Resolves a user entity by unique ID.
     */
    @Transactional(readOnly = true)
    public Optional<UserAccount> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    /**
     * Checks if a user exists by unique ID.
     */
    @Transactional(readOnly = true)
    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Upgrades or updates an account's security role (e.g., when a subscription is activated).
     */
    @Transactional
    public void upgradeUserRole(UUID userId, UserRole newRole) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        log.info("Upgrading user [{}] role from [{}] to [{}]", userId, user.getRole(), newRole);
        user.setRole(newRole);
        userRepository.save(user);
    }
}
