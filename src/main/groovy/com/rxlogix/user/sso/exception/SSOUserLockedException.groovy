package com.rxlogix.user.sso.exception;

import org.springframework.security.authentication.LockedException;

public class SSOUserLockedException extends LockedException implements SSOAuthException {

    public SSOUserLockedException(String msg){
        super(msg);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // do nothing
        return this;
    }
}
