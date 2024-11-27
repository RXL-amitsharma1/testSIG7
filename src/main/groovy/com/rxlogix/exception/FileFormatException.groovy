package com.rxlogix.exception

import java.io.IOException

class FileFormatException extends IOException {
    FileFormatException(String message) {
        super(message)
    }

    FileFormatException() {
        super()
    }
}