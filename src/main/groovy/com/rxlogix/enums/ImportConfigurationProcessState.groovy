package com.rxlogix.enums

enum ImportConfigurationProcessState {

    IN_PROCESS('In Progress'),
    IN_READ('Scheduled'),
    SUCCESS('Success'),
    FAILED('Failed')

    private final String val

    ImportConfigurationProcessState(String val) {
        this.val = val
    }

    String value() { return val }

}