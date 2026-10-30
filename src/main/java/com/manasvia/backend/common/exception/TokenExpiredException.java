package com.manasvia.backend.common.exception;

/**
 * Thrown when an incoming refresh token has exceeded its expiration lifespan.
 */
public class TokenExpiredException extends AuthenticationFailedException {

    public TokenExpiredException(String message) {
        super("TOKEN_EXPIRED", message);
    }
}
