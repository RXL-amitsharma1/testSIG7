package com.rxlogix.enums

enum SkippedAlertStateEnum {

    CREATED('CREATED'),
    READY_FOR_EXECUTION('READY_FOR_EXECUTION'),
    EXECUTING('EXECUTING'),
    FAILED('FAILED'),
    EXECUTED('EXECUTED'),
    DISCARDED('DISCARDED')

    private final String val

    SkippedAlertStateEnum(String val) {
        this.val = val
    }

    String value() { return val }
}