package com.rxlogix.exception

abstract class ClientErrorException extends RestApiException {

    ClientErrorException() {
        super()
    }

    ClientErrorException(String message) {
        super(message)
    }

    @Override
    int getResultCode() {
        return 400
    }

    @Override
    String getStatus() {
        return 'TXN_FAILURE'
    }
}
