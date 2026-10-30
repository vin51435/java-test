package com.manasvia.backend.common.exception;

/**
 * Thrown when an authentication attempt is made against a deactivated or suspended user account.
 */
public class AccountInactiveException extends AuthenticationFailedException {

    public AccountInactiveException(String message) {
        super("ACCOUNT_INACTIVE", message);
    }
}
