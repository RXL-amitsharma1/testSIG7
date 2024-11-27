package com.rxlogix.enums

enum ProdAssignmentProcessState {

    IN_PROCESS('In Progress'),
    IN_READ('Scheduled'),
    SUCCESS('Success'),
    FAILED('Failed')

    private final String val

    ProdAssignmentProcessState(String val) {
        this.val = val
    }

    String value() { return val }
}