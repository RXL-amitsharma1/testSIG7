package com.rxlogix.signal

class ValidatedSingleCaseAlertMapping implements Serializable {

    ValidatedSignal validatedSignal
    SingleCaseAlert singleCaseAlert

    static mapping = {
        table("VALIDATED_SINGLE_ALERTS")
        singleCaseAlert column: "SINGLE_ALERT_ID"
        validatedSignal column: "VALIDATED_SIGNAL_ID"
        version false
        id composite:["validatedSignal","singleCaseAlert"]
    }

    static constraints = {
    }
}
