package com.rxlogix.exception

class ResourceNotFoundException extends ClientErrorException {
    private String messageCode = 'restApi.error.notFound'

    ResourceNotFoundException() {
        super()
    }

    ResourceNotFoundException(String message) {
        super(message)
    }

    @Override
    int getResultCode() {
        return 404
    }

    @Override
    String getMessageCode() {
        return messageCode
    }

    @Override
    String getStatus() {
        return 'NO_RECORD_FOUND'
    }

    static ResourceNotFoundException withMessageCode(String messageCode, String message = null) {
        def exception = new ResourceNotFoundException(message)
        exception.messageCode = messageCode
        exception
    }
}
