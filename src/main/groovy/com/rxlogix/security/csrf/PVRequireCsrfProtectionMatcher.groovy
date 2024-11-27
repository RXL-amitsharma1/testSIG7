package com.rxlogix.security.csrf

import grails.util.Holders
import org.springframework.security.web.util.matcher.RequestMatcher

import javax.servlet.http.HttpServletRequest
import java.util.regex.Pattern

class PVRequireCsrfProtectionMatcher implements RequestMatcher {
    private Pattern allowedMethods;

    private PVRequireCsrfProtectionMatcher() {
        this.allowedMethods = Pattern.compile(/^(GET|HEAD|TRACE|OPTIONS)$/);
    }

    @Override
    boolean matches(HttpServletRequest request) {
        String requestPath = request.getServletPath()
        return !((allowedMethods.matcher(request.getMethod()).matches()) ||
                (Holders.config.pvsignal.csrfProtection.excludeURLPatterns?.any { requestPath.contains(it) })
        )
    }
}
