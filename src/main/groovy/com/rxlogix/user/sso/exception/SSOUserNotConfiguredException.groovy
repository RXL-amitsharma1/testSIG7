package com.rxlogix.user.sso.exception;

import org.springframework.security.authentication.AccountStatusException;

public class SSOUserNotConfiguredException extends AccountStatusException implements SSOAuthException {
    private static final long serialVersionUID = 1;

    public SSOUserNotConfiguredException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // do nothing
        return this;
    }
}
