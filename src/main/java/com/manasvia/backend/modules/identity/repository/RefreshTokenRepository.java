package com.manasvia.backend.modules.identity.repository;

import com.manasvia.backend.modules.identity.model.RefreshToken;
import com.manasvia.backend.modules.identity.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access operations for {@link RefreshToken} entities.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUser(UserAccount user);

    void deleteByUser(UserAccount user);
}
