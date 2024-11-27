package com.rxlogix.signal

class DynamicField {

    static auditable = false

    String fieldName
    String fieldLabel
    String fieldType
    Boolean isEnabled
    Integer sequence

    static constraints = {
        fieldName nullable: false
        fieldLabel nullable: false
        fieldType nullable: false
        isEnabled nullable: false
        sequence nullable: false
    }

    static mapping = {
        table name: "dynamic_fields"
    }
}
