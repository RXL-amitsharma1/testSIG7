package com.rxlogix.customException


class ExecutionStatusException extends Throwable {
    String errorCause

    ExecutionStatusException() {
    }

    ExecutionStatusException(String errorCause) {
        this.errorCause = errorCause
    }
}
