package com.manasvia.backend.modules.identity.api;

import com.manasvia.backend.modules.identity.model.UserAccount;
import com.manasvia.backend.common.security.UserRole;
import com.manasvia.backend.modules.identity.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class IdentityInternalApiTest {

    @Autowired
    private IdentityInternalApi identityInternalApi;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("IdentityInternalApi: findUserById & existsById return accurate summary for existing user")
    void testFindUserByIdAndExistsById() {
        UserAccount user = UserAccount.builder()
                .deviceFingerprint("api-test-device-" + UUID.randomUUID())
                .role(UserRole.GUEST)
                .active(true)
                .build();
        user = userRepository.save(user);

        assertThat(identityInternalApi.existsById(user.getId())).isTrue();
        assertThat(identityInternalApi.existsById(UUID.randomUUID())).isFalse();

        Optional<UserSummaryDto> summaryOpt = identityInternalApi.findUserById(user.getId());
        assertThat(summaryOpt).isPresent();
        assertThat(summaryOpt.get().id()).isEqualTo(user.getId());
        assertThat(summaryOpt.get().role()).isEqualTo(UserRole.GUEST);
        assertThat(summaryOpt.get().active()).isTrue();
    }

    @Test
    @DisplayName("IdentityInternalApi: upgradeUserRole successfully transitions role")
    void testUpgradeUserRole() {
        UserAccount user = UserAccount.builder()
                .deviceFingerprint("upgrade-test-" + UUID.randomUUID())
                .role(UserRole.GUEST)
                .active(true)
                .build();
        user = userRepository.save(user);

        identityInternalApi.upgradeUserRole(user.getId(), UserRole.PREMIUM);

        UserAccount updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getRole()).isEqualTo(UserRole.PREMIUM);
    }
}
