package com.rxlogix.user.sso.exception

import org.springframework.security.authentication.AccountStatusException

public class SSOUserAccountExpiredException extends AccountStatusException implements SSOAuthException {

    public SSOUserAccountExpiredException(String msg){
        super(msg)
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this
    }
}
