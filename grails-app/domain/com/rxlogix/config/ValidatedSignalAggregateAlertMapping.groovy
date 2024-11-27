package com.rxlogix.config

import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ValidatedSignal

class ValidatedSignalAggregateAlertMapping implements Serializable {
    ValidatedSignal validatedSignal
    AggregateCaseAlert aggregateCaseAlert
    Date dateCreated
    Boolean autoRouted

    static mapping = {
        table name: 'VALIDATED_AGG_ALERTS'
        validatedSignal column: 'VALIDATED_SIGNAL_ID'
        aggregateCaseAlert column: 'AGG_ALERT_ID'
        version false
        id composite: ['validatedSignal', 'aggregateCaseAlert']
    }
    static constraints = {
        dateCreated nullable: true
        autoRouted nullable: true
    }
}
