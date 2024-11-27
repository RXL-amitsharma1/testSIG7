package com.rxlogix.signal

class DynamicDropdownValue {

    static auditable = false

    String fieldName
    String fieldKey
    String fieldValue
    Boolean isEnabled

    static constraints = {
        fieldName nullable: false
        fieldKey nullable: false
        fieldValue nullable: false
        isEnabled nullable: false
    }

    static mapping = {
        table name: "dynamic_dropdown_values"
    }
}
