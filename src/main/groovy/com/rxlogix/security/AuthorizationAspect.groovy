package com.rxlogix.security

import com.rxlogix.Constants
import grails.plugin.springsecurity.SpringSecurityService
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder

import javax.servlet.http.HttpServletResponse

@Slf4j
@Aspect
class AuthorizationAspect {
    private static final String ADMIN = 'ROLE_ADMIN'
    @Autowired
    private SpringSecurityService springSecurityService
    @Autowired
    private HttpServletResponse response

    @Around('execution(* *(..)) && (@annotation(authorize) || @within(authorize))')
    Object checkRequiredRole(ProceedingJoinPoint joinPoint, Authorize authorize) {
        def requiredAuthorities = Optional.ofNullable(authorize).orElseGet { ->
            def signature = joinPoint.getSignature() as MethodSignature
            signature.getMethod().getAnnotation(Authorize.class);
        }.value().toList()
        def userAuthorities = springSecurityService.authentication.authorities
                .collect { it.authority }

        if (!springSecurityService.isLoggedIn()) {
            writeErrorResponse(401, 'TXN_FAILURE', 'Unauthorized')
            log.info('Authorization failed. User is not authenticated.')
            return null
        }
        if (!hasRequiredRole(userAuthorities, requiredAuthorities)) {
            writeErrorResponse(403, 'TXN_FORBIDDEN', 'Forbidden')
            log.info('Authorization failed. User does not have required role.')
            return null
        }
        log.info('User successfully authorized')
        return joinPoint.proceed()
    }

    private boolean hasRequiredRole(Collection<String> userAuthorities, Collection<String> requiredAuthorities) {
        log.debug('User roles: {}. Required roles: {}.')
        return requiredAuthorities.isEmpty() ||
                userAuthorities.contains(ADMIN) ||
                requiredAuthorities.any { userAuthorities.contains(it) }
    }

    private void writeErrorResponse(int code, String status, String message) {
        def responseBody = [(Constants.RESULT_CODE)   : code,
                            (Constants.RESULT_STATUS) : status,
                            (Constants.RESULT_MESSAGE): message]

        response.status = code
        response.contentType = 'application/json'
        response.writer.write(new JsonBuilder(responseBody).toString())
        response.writer.flush()
    }
}