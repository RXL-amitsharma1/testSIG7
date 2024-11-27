
package com.rxlogix.enums

enum DispositionEnum {
    VALIDATED('Valid Observation'),
    NON_VALIDATED('Non-Valid Observation'),
    REFUTED('Refuted Signal'),
    CONFIRMED('Confirmed Signal'),
    NEW('New'),
    NO_SIGNAL('No Signal'),
    VALID_SIGNAL('Validated Signal'),
    REFUTED_AND_CONFIRMED('Refuted Signal & Confirmed Signal'),
    VALID_AND_NO_SIGNAL('Valid Signal & No Signal'),
    VALID_AND_CONFIRMED('Valid Signal & Confirmed Signal'),
    NEW_EVENT('New Event'),
    CLOSED_MONITORING('Closed Monitoring'),
    CONTINUE_MONITORING('Continued Monitoring'),
    SAFETY_TOPIC('Safety Topic')


    final String val
    DispositionEnum(String val){
        this.val = val
    }

    String value() { return val }

}


