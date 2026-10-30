package com.manasvia.backend.modules.identity.service;

import com.manasvia.backend.common.exception.AccountInactiveException;
import com.manasvia.backend.common.exception.InvalidTokenException;
import com.manasvia.backend.common.exception.TokenExpiredException;
import com.manasvia.backend.common.exception.TokenRevokedException;
import com.manasvia.backend.common.security.JwtService;
import com.manasvia.backend.common.security.UserRole;
import com.manasvia.backend.modules.identity.dto.GuestAuthRequest;
import com.manasvia.backend.modules.identity.dto.RefreshTokenRequest;
import com.manasvia.backend.modules.identity.dto.TokenRefreshResponse;
import com.manasvia.backend.modules.identity.dto.TokenResponse;
import com.manasvia.backend.modules.identity.model.RefreshToken;
import com.manasvia.backend.modules.identity.model.UserAccount;
import com.manasvia.backend.modules.identity.repository.RefreshTokenRepository;
import com.manasvia.backend.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Core business orchestration for authentication, guest onboarding, and session token lifecycle.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    /**
     * Authenticates an anonymous guest device.
     * If the device fingerprint already exists, resolves the existing account.
     * Otherwise, creates a brand-new guest user account and persists session tokens.
     */
    @Transactional
    public TokenResponse authenticateGuest(GuestAuthRequest request) {
        final String fingerprint = request.getDeviceFingerprint().trim();

        UserAccount user = userRepository.findByDeviceFingerprint(fingerprint)
                .map(existing -> {
                    log.info("Existing guest device resolved: [id={}, role={}]", existing.getId(), existing.getRole());
                    return existing;
                })
                .orElseGet(() -> {
                    log.info("Registering new guest account for device fingerprint prefix: [{}...]",
                            fingerprint.substring(0, Math.min(8, fingerprint.length())));
                    UserAccount newAccount = UserAccount.createGuest(fingerprint);
                    return userRepository.save(newAccount);
                });

        // Generate stateless JWT access token
        String accessToken = jwtService.generateGuestToken(user.getId());

        // Generate secure 60-day refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plus(60, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        long expiresInSeconds = jwtService.getGuestTokenExpirationMs() / 1000;

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(expiresInSeconds)
                .userId(user.getId())
                .role(user.getRole())
                .build();
    }

    /**
     * Refreshes an expired access token using a valid, non-revoked refresh token.
     */
    @Transactional
    public TokenRefreshResponse refreshAccessToken(RefreshTokenRequest request) {
        final String rawToken = request.getRefreshToken().trim();
        RefreshToken refreshToken = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new TokenRevokedException("Refresh token has been revoked");
        }
        if (refreshToken.isExpired()) {
            throw new TokenExpiredException("Refresh token has expired");
        }

        UserAccount user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new AccountInactiveException("User account is inactive");
        }

        String newAccessToken = user.getRole() == UserRole.GUEST
                ? jwtService.generateGuestToken(user.getId())
                : jwtService.generateToken(user.getId(), user.getRole());

        long expiresInSeconds = (user.getRole() == UserRole.GUEST
                ? jwtService.getGuestTokenExpirationMs()
                : jwtService.getAccessTokenExpirationMs()) / 1000;

        log.info("Access token successfully refreshed for user [{}]", user.getId());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(expiresInSeconds)
                .build();
    }
}
