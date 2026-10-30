package com.manasvia.backend.common.exception;

/**
 * Thrown when an incoming refresh token has been explicitly revoked.
 */
public class TokenRevokedException extends AuthenticationFailedException {

    public TokenRevokedException(String message) {
        super("TOKEN_REVOKED", message);
    }
}
