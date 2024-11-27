package com.rxlogix.signal

class AdHocAlertType {
    static auditable = true

    String value
    boolean display = true
    String description
    String displayName
    String displayName_local
    String description_local

    static constraints = {
        value(unique: true, blank: false)
        description(nullable: true)
        displayName nullable: true
        description_local nullable: true
        displayName_local nullable: true
    }

    static mapping = {
        table name: "AEVAL_TYPE"
    }
}
