package com.rxlogix.user.sso.exception;

import org.springframework.security.authentication.DisabledException;

public class SSOUserDisabledException extends DisabledException implements SSOAuthException {

    public SSOUserDisabledException(String msg) {
        super(msg);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // do nothing
        return this;
    }
}
