package com.manasvia.backend.common.exception;

/**
 * Thrown when an invalid, non-existent, or malformed authentication or refresh token is received.
 */
public class InvalidTokenException extends AuthenticationFailedException {

    public InvalidTokenException(String message) {
        super("INVALID_TOKEN", message);
    }
}
