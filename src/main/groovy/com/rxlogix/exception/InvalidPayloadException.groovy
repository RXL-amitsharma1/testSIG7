package com.rxlogix.exception

class InvalidPayloadException extends ClientErrorException {
    private String messageCode = 'restApi.error.invalidPayload'

    InvalidPayloadException() {
        super()
    }

    InvalidPayloadException(String message) {
        super(message)
    }

    @Override
    String getMessageCode() {
        messageCode
    }

    static InvalidPayloadException withMessageCode(String messageCode, String message = null) {
        def exception = new InvalidPayloadException(message)
        exception.messageCode = messageCode
        exception
    }
}
