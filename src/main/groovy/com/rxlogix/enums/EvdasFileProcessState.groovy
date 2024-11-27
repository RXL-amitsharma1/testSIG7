package com.rxlogix.enums

enum EvdasFileProcessState {

    IN_PROCESS('PROCESSING'),
    SUCCESS('SUCCESS'),
    FAILED('FAILED')

    private final String val

    EvdasFileProcessState(String val) {
        this.val = val
    }

    String value() { return val }
}