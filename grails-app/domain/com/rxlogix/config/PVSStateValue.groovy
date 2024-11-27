package com.rxlogix.config

enum PVSStateValue {

    New("NewObservation"),
    RequiresReview("RequiresReview"),
    Validation("Validation"),
    Confirmation("Confirmation"),
    ContinueMonitoring("ContinueMonitoring"),
    CommunicationAndRiskMinimizationAction("CommunicationAndRiskMinimizationAction"),
    Closed("Closed"),
    ClosedSignal("ClosedSignal"),
    ClosedObservation('ClosedObservation')

    String value
    String description

    PVSStateValue(name) {value = name}

    @Override
    def String toString() {value}

    def getId() {value}
}