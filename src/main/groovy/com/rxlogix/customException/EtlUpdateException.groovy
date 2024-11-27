package com.rxlogix.customException

class EtlUpdateException extends Exception {

    EtlUpdateException() {
    }

    EtlUpdateException(String message) {
        super(message)
    }
}
