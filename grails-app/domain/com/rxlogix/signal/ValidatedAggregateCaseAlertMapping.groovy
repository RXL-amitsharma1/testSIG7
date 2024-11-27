package com.rxlogix.signal

class ValidatedAggregateCaseAlertMapping implements Serializable {

    ValidatedSignal validatedSignal
    AggregateCaseAlert aggregateCaseAlert
    boolean isCarryForward = false

    static mapping = {
        table("VALIDATED_AGG_ALERTS")
        aggregateCaseAlert column: "AGG_ALERT_ID"
        validatedSignal column: "VALIDATED_SIGNAL_ID"
        version false
        id composite:["validatedSignal","aggregateCaseAlert"]
    }

    static constraints = {
        isCarryForward nullable: true
    }
}
