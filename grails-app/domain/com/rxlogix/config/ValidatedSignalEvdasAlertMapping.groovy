package com.rxlogix.config

import com.rxlogix.signal.ValidatedSignal

class ValidatedSignalEvdasAlertMapping implements Serializable {
    ValidatedSignal validatedSignal
    EvdasAlert evdasAlert
    boolean isCarryForward = false
    Date dateCreated
    Boolean autoRouted

    static mapping = {
        table name: 'VALIDATED_EVDAS_ALERTS'
        validatedSignal column: 'VALIDATED_SIGNAL_ID'
        evdasAlert column: 'EVDAS_ALERT_ID'
        version false
        id composite: ['validatedSignal', 'evdasAlert']
    }
    static constraints = {
        isCarryForward nullable: true
        dateCreated nullable: true
        autoRouted nullable: true
    }
}
