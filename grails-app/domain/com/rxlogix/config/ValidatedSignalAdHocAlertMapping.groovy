package com.rxlogix.config

import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.ValidatedSignal

class ValidatedSignalAdHocAlertMapping implements Serializable {
    ValidatedSignal validatedSignal
    AdHocAlert adHocAlert

    static mapping = {
        table name: 'VALIDATED_ADHOC_ALERTS'
        validatedSignal column: 'VALIDATED_SIGNAL_ID'
        adHocAlert column: 'ADHOC_ALERT_ID'
        version false
        id composite: ['validatedSignal', 'adHocAlert']
    }

    static constraints = {
    }
}
