package com.rxlogix.config

class PVConcept {
    static auditable = false

    String name

    static belongsTo = [signalStrategy: SignalStrategy]

    static constraints = {
        name(unique: true, blank: false)
    }

    static mapping = {
        table name: "PV_CONCEPT"
        name column: "NAME"
    }
}
