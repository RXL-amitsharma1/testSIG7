package com.rxlogix.config

enum DispositionValue {

    NewPotentialSignal("NewPotentialSignal"),
    NonValid("NonValid"),
    ValidatedSignal("ValidatedSignal"),
    ConfirmedValidatedSignal("ConfirmedValidatedSignal"),
    NonConfirmed("NonConfirmed"),
    FurtherEvaluation("FurtherEvaluation"),
    ConfirmedValidatedSignalAndRecommendation("ConfirmedValidatedSignalAndRecommendation"),
    NonConfirmedSignalAndFurtherEvaluation("NonConfirmedSignalAndFurtherEvaluation"),
    ConfirmedRisk("ConfirmedRisk"),
    Closed("Closed"),
    ClosedNonValidSignal("ClosedNonValidSignal")

    def String value
    def description

    DispositionValue(v) {value = v}

    @Override
    def String toString() {value}

    def getId() {value}
}