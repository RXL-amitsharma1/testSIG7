package com.rxlogix.security.jwt

import com.rxlogix.Constants
import com.rxlogix.user.CustomUserDetails
import grails.config.Config
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Slf4j
class JwtAuthenticationFilter extends GenericFilterBean {
    private static final String BEARER_PREFIX = 'Bearer '
    private static final String AUTHORIZATION_HEADER = 'Authorization'
    JwtService jwtService

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {

        def httpRequest = (HttpServletRequest) request
        def httpResponse = (HttpServletResponse) response
        def authHeader = httpRequest.getHeader(AUTHORIZATION_HEADER)
        if (authHeader?.startsWith(BEARER_PREFIX)) {
            try {
                def token = authHeader.substring(BEARER_PREFIX.length())
                def userDetails = buildUserDetails(jwtService.validate(token))
                def authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                SecurityContextHolder.context.setAuthentication(authentication)
                log.info('User authenticated as {}', userDetails.username)
            } catch (Exception e) {
                log.error('Failed to authenticate via JwtAuthenticationFilter so breaking further filter chain', e)
                writeErrorResponse(httpResponse)
                return
            }
        }
        chain.doFilter(request, response)
    }

    private UserDetails buildUserDetails(Jws<Claims> decodedToken) {
        def claims = decodedToken.body
        def username = claims.getSubject()
        String fullName = claims.get(JwtService.CLAIM_FULL_NAME).toString()
        Long userId = claims.get(JwtService.CLAIM_USER_ID).toString()?.toLong()
        List<GrantedAuthority> authorities = claims.get(JwtService.CLAIM_AUTHORITIES).split(',')
                .collect { String authority -> new SimpleGrantedAuthority(authority) }

        new CustomUserDetails(username, authorities, userId, fullName)
    }

    private void writeErrorResponse(HttpServletResponse httpResponse) {
        httpResponse.status = 401
        def responseBody = [(Constants.RESULT_CODE): 401,
                            (Constants.RESULT_STATUS): Constants.TXN_FAILURE[Constants.RESULT_STATUS],
                            (Constants.RESULT_MESSAGE): 'Unauthorized']

        httpResponse.contentType = 'application/json'
        httpResponse.writer.write(new JsonBuilder(responseBody).toString())
        httpResponse.writer.flush()
    }
}
