package com.manasvia.backend.common.security;

import com.manasvia.backend.modules.identity.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Service responsible for creating, cryptographically signing, parsing, and validating
 * stateless JSON Web Tokens (JWT) using the HMAC-SHA256 (HS256) algorithm.
 */
@Service
@Getter
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final String secret;
    private final long accessTokenExpirationMs;
    private final long guestTokenExpirationMs;

    public JwtService(
            @Value("${manasvia.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secret,
            @Value("${manasvia.jwt.access-token-expiration-ms:86400000}") long accessTokenExpirationMs,
            @Value("${manasvia.jwt.guest-token-expiration-ms:2592000000}") long guestTokenExpirationMs
    ) {
        this.secret = secret;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.guestTokenExpirationMs = guestTokenExpirationMs;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a standard authenticated access token (24-hour default).
     */
    public String generateToken(UUID userId, UserRole role) {
        return generateToken(userId, role, Map.of(), accessTokenExpirationMs);
    }

    /**
     * Generates a 30-day extended access token specifically tailored for anonymous guests.
     */
    public String generateGuestToken(UUID userId) {
        return generateToken(userId, UserRole.GUEST, Map.of(), guestTokenExpirationMs);
    }

    /**
     * Generates a signed HS256 JWT with custom extra claims and configurable expiration.
     */
    public String generateToken(UUID userId, UserRole role, Map<String, Object> extraClaims, long expirationMs) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expirationMs, ChronoUnit.MILLIS);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the Subject claim (User UUID) from the token.
     */
    public UUID extractUserId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return UUID.fromString(subject);
    }

    /**
     * Extracts the UserRole claim from the token.
     */
    public UserRole extractRole(String token) {
        Claims claims = extractAllClaims(token);
        String roleStr = claims.get("role", String.class);
        return roleStr != null ? UserRole.valueOf(roleStr) : UserRole.GUEST;
    }

    /**
     * Extracts token expiration timestamp.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a single claim using a resolver function.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses and cryptographically validates all claims in the JWT payload.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validates whether a token is cryptographically sound and unexpired.
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
