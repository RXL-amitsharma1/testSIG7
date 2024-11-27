package com.rxlogix.user.sso.exception

import org.springframework.security.authentication.InternalAuthenticationServiceException

public class SSOConfigurationException extends InternalAuthenticationServiceException implements SSOAuthException {

    public SSOConfigurationException(String msg) {
        super(msg);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // do nothing
        return this;
    }

}