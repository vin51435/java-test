package com.manasvia.backend.common.exception;

/**
 * Base domain exception representing authentication and token lifecycle failures.
 * Encapsulates a distinct error code for unambiguous client handling.
 */
public class AuthenticationFailedException extends RuntimeException {

    private final String errorCode;

    public AuthenticationFailedException(String message) {
        super(message);
        this.errorCode = "AUTHENTICATION_FAILED";
    }

    public AuthenticationFailedException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
