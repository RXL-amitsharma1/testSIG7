package com.rxlogix.signal

import com.rxlogix.config.AlertDocument

class ValidatedAlertDocument implements Serializable {

    ValidatedSignal validatedSignal
    AlertDocument alertDocument

    static mapping = {
        table("VALIDATED_ALERT_DOCUMENTS")
        validatedSignal column: "VALIDATED_SIGNAL_ID"
        alertDocument column: "ALERT_DOCUMENT_ID"
        version false
        id composite : ["validatedSignal", "alertDocument"]
    }

}