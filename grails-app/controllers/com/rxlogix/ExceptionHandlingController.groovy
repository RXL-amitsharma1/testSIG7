package com.rxlogix


import com.rxlogix.exception.RestApiException
import grails.converters.JSON
import groovy.util.logging.Slf4j
import io.jsonwebtoken.JwtException

@Slf4j
trait ExceptionHandlingController {
   private static final String ERROR_MESSAGE_TEMPLATE = 'An exception occurred while calling {}'

    def handleException(Exception e) {
        log.error(ERROR_MESSAGE_TEMPLATE, request.getRequestURI(), e)
        interpolatedErrorResponse(500, 'INTERNAL SERVER ERROR', 'restApi.error.serverError')
    }

    def handleClientErrorException(RestApiException e) {
        log.info(ERROR_MESSAGE_TEMPLATE, request.getRequestURI(), e)
        interpolatedErrorResponse(e.getResultCode(), e.getStatus(), e.getMessageCode())
    }

    def handleJwtException(JwtException e) {
        log.info(ERROR_MESSAGE_TEMPLATE, request.getRequestURI(), e)
        interpolatedErrorResponse(401, 'TXN_FAILURE', 'restApi.error.unauthorized')
    }

    def handleCustomException(Integer code, String status, String message) {
        interpolatedErrorResponse(code, status, message)
    }

    private def interpolatedErrorResponse(int code, String status, String messageCode) {
        errorResponse(code, status, message(code: messageCode))
    }

    private def errorResponse(int code, String status, String message) {
        response.status = code
        def responseBody = [(Constants.RESULT_CODE)   : code,
                            (Constants.RESULT_STATUS) : status,
                            (Constants.RESULT_MESSAGE): message]
        render responseBody as JSON
    }
}