package com.rxlogix.security
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.util.Assert
import org.springframework.security.core.session.SessionRegistry

class CustomSessionLogoutHandler implements LogoutHandler{
    private final SessionRegistry sessionRegistry

    public CustomSessionLogoutHandler(SessionRegistry sessionRegistry) {
        Assert.notNull(sessionRegistry, "sessionRegistry cannot be null");
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    void logout(HttpServletRequest httpServletRequest,
                HttpServletResponse httpServletResponse,
                Authentication authentication) {
        //We need to remove from User session from SessionRegistry to make sure session removed off
        this.sessionRegistry.removeSessionInformation(httpServletRequest.getSession().getId())
    }

}
