package com.rxlogix.config

import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.ValidatedSignal

class ValidatedSignalSingleAlertMapping implements Serializable {
    ValidatedSignal validatedSignal
    SingleCaseAlert singleCaseAlert
    boolean isCarryForward = false
    Date dateCreated

    static mapping = {
        table name: 'VALIDATED_SINGLE_ALERTS'
        validatedSignal column: 'VALIDATED_SIGNAL_ID'
        singleCaseAlert column: 'SINGLE_ALERT_ID'
        version false
        id composite: ['validatedSignal', 'singleCaseAlert']
    }
    static constraints = {
        isCarryForward nullable: true
        dateCreated nullable: true
    }
}
