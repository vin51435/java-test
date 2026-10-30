package com.manasvia.backend.modules.identity.model;

import com.manasvia.backend.common.security.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserAccountTest {

    @Test
    @DisplayName("createGuest() factory method should correctly initialize default fields")
    void testCreateGuestFactory() {
        String fingerprint = "test-fp-123";
        UserAccount guest = UserAccount.createGuest(fingerprint);

        assertThat(guest.getDeviceFingerprint()).isEqualTo(fingerprint);
        assertThat(guest.getRole()).isEqualTo(UserRole.GUEST);
        assertThat(guest.isActive()).isTrue();
        assertThat(guest.getId()).isNull();
        assertThat(guest.getEmail()).isNull();
    }

    @Test
    @DisplayName("equals and hashCode contract should be based on ID equality")
    void testEqualsAndHashCode() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        UserAccount userA = UserAccount.builder().id(id1).deviceFingerprint("fp1").build();
        UserAccount userB = UserAccount.builder().id(id1).deviceFingerprint("fp2").build();
        UserAccount userC = UserAccount.builder().id(id2).deviceFingerprint("fp1").build();

        // Reflexive
        assertThat(userA).isEqualTo(userA);

        // Symmetric (same ID = equal regardless of other fields)
        assertThat(userA).isEqualTo(userB);
        assertThat(userB).isEqualTo(userA);
        assertThat(userA.hashCode()).isEqualTo(userB.hashCode());

        // Different ID = not equal
        assertThat(userA).isNotEqualTo(userC);

        // Null comparison and different type
        assertThat(userA).isNotEqualTo(null);
        assertThat(userA).isNotEqualTo("some-string");

        // Null IDs
        UserAccount userNullId1 = new UserAccount();
        UserAccount userNullId2 = new UserAccount();
        assertThat(userNullId1).isNotEqualTo(userNullId2);
    }

    @Test
    @DisplayName("Getters and Setters test for full field coverage")
    void testGettersAndSetters() {
        UserAccount user = new UserAccount();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        user.setId(id);
        user.setDeviceFingerprint("fp-xyz");
        user.setEmail("user@example.com");
        user.setPasswordHash("hashed_pw");
        user.setGoogleId("google-sub-id");
        user.setDisplayName("Test User");
        user.setEmailVerifiedAt(now);
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getDeviceFingerprint()).isEqualTo("fp-xyz");
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashed_pw");
        assertThat(user.getGoogleId()).isEqualTo("google-sub-id");
        assertThat(user.getDisplayName()).isEqualTo("Test User");
        assertThat(user.getEmailVerifiedAt()).isEqualTo(now);
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }
}
