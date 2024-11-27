package com.rxlogix

import org.springframework.security.web.savedrequest.HttpSessionRequestCache

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomHttpSessionRequestCache extends HttpSessionRequestCache {
    void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        if(!"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            super.saveRequest(request, response)
        }
    }
}
