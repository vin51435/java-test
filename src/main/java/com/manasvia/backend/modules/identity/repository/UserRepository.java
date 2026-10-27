package com.manasvia.backend.modules.identity.repository;

import com.manasvia.backend.modules.identity.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access operations for {@link UserAccount} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByDeviceFingerprint(String deviceFingerprint);

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByGoogleId(String googleId);

    boolean existsByDeviceFingerprint(String deviceFingerprint);
}
