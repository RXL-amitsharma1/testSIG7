package com.rxlogix.exception

abstract class RestApiException extends RuntimeException {
    RestApiException() {
        super()
    }

    RestApiException(String message) {
        super(message)
    }

    abstract int getResultCode()
    abstract String getStatus()
    abstract String getMessageCode()
}
