package com.rxlogix.api

import com.rxlogix.ExceptionHandlingController
import com.rxlogix.exception.InvalidPayloadException
import com.rxlogix.security.Authorize
import com.rxlogix.security.jwt.JwtService
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON

import org.springframework.security.core.context.SecurityContextHolder

class JwtAuthController implements ExceptionHandlingController {
    JwtService jwtService

    static allowedMethods = [accessToken: "POST", refresh: "POST"]

    @Authorize
    def accessToken() {
        def userDetails = SecurityContextHolder.context
                .authentication
                .principal
        def claims = [(JwtService.CLAIM_USER_ID)  : userDetails.id,
                      (JwtService.CLAIM_FULL_NAME): userDetails.fullName]
        def accessToken = jwtService.getAccessToken(userDetails.username, userDetails.authorities, claims)

        render RestApiResponse.successResponseWithData(accessToken) as JSON
    }

    def refresh() {
        def refreshToken = request.JSON?.refreshToken
        if (!refreshToken) {
            throw new InvalidPayloadException()
        }
        def accessToken = jwtService.refreshAccessToken(refreshToken)
        
        render RestApiResponse.successResponseWithData(accessToken) as JSON
    }
}