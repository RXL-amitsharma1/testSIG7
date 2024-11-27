package com.rxlogix.enums

enum JustificationFeatureEnum {

    alertWorkflow("Alert Workflow"),
    signalWorkflow("Signal Workflow"),
    signalPriority("Signal Priority"),
    alertPriority("Alert Priority"),
    caseAddition("Case Addition"),

    final String val

    JustificationFeatureEnum(String val) {
        this.val = val
    }

    String value() { return val }

    static List<JustificationFeatureEnum> getAll() {
        [alertWorkflow, signalWorkflow, caseAddition, alertPriority, signalPriority]
    }
}