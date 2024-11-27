package com.rxlogix.security

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j
@CompileStatic
class SecurityHeadersFilter implements Filter, GrailsConfigurationAware {

    List<String> allowedDomains
    Boolean strictTransportEnabled

    void setConfiguration(Config cfg) {
        allowedDomains = cfg.getProperty('grails.cors.allowedOrigins', List, [])
        strictTransportEnabled = cfg.getProperty('pvs.strict.transport.security.enabled', Boolean, false)
    }

    @Override
    void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        filterHeader(request as HttpServletRequest, response as HttpServletResponse)
        chain.doFilter(request, response);
    }

    @Override
    void destroy() {

    }

    protected void filterHeader(final HttpServletRequest request, final HttpServletResponse response) {
        /**
         * Prevent Cacheable HTTPS Response
         */
        response.setHeader("Cache-Control", "no-cache, no-store")
        response.setHeader("Pragma", "no-cache")
        response.setHeader('X-XSS-Protection', '1; mode=block')
        response.setHeader('X-Content-Type-Options', 'nosniff')
        response.setHeader('Access-Control-Allow-Credentials', 'true')
        response.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, PATCH, DELETE, OPTIONS')
        response.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        response.setHeader('Access-Control-Expose-Headers', 'Content-Disposition, Content-Length, X-Total-Count')

        if (strictTransportEnabled && (request.isSecure() || request.getHeader('X-Forwarded-Proto')?.toLowerCase() == 'https')) {

            log.trace("===========Strict-Transport-Security=============")
            response.setHeader('Strict-Transport-Security', "max-age=31536000;includeSubDomains")

        }
        String originDomain = request.getHeader('Origin')

        if (originDomain in allowedDomains) {

            response.setHeader('Access-Control-Allow-Origin', originDomain)

        }

    }

}