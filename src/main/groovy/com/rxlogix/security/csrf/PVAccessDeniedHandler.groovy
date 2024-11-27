package com.rxlogix.security.csrf

import groovy.util.logging.Slf4j
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.access.AccessDeniedHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Slf4j
class PVAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    void handle(HttpServletRequest request,
                HttpServletResponse response,
                AccessDeniedException exc) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication()
        if (auth != null) {
            log.warn("User: " + auth.getName()
                    + " attempted to access the protected URL: "
                    + request.getRequestURI())
        }

        response.sendRedirect(request.getContextPath() + "/login/auth")
    }
}
