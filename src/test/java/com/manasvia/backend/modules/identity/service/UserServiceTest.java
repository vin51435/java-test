package com.manasvia.backend.modules.identity.service;

import com.manasvia.backend.common.security.UserRole;
import com.manasvia.backend.modules.identity.dto.UserProfileResponse;
import com.manasvia.backend.modules.identity.model.UserAccount;
import com.manasvia.backend.modules.identity.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("getUserProfile should return mapped DTO when user exists")
    void testGetUserProfileSuccess() {
        UUID userId = UUID.randomUUID();
        UserAccount account = UserAccount.builder()
                .id(userId)
                .deviceFingerprint("fp-mock")
                .role(UserRole.GUEST)
                .active(true)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(account));

        UserProfileResponse profile = userService.getUserProfile(userId);

        assertThat(profile.getId()).isEqualTo(userId);
        assertThat(profile.getDeviceFingerprint()).isEqualTo("fp-mock");
        assertThat(profile.getRole()).isEqualTo(UserRole.GUEST);
        assertThat(profile.isActive()).isTrue();
    }

    @Test
    @DisplayName("getUserProfile should throw IllegalArgumentException when user does not exist")
    void testGetUserProfileNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("findById and existsById queries delegate to repository")
    void testFindByIdAndExistsById() {
        UUID userId = UUID.randomUUID();
        UserAccount account = UserAccount.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(account));
        when(userRepository.existsById(userId)).thenReturn(true);

        assertThat(userService.findById(userId)).isPresent();
        assertThat(userService.existsById(userId)).isTrue();

        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());
        when(userRepository.existsById(unknownId)).thenReturn(false);

        assertThat(userService.findById(unknownId)).isEmpty();
        assertThat(userService.existsById(unknownId)).isFalse();
    }
}
