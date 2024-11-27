package com.rxlogix.config

class CustomSQLTemplateValue extends ParameterValue {
    static auditable = false
    String field

    static mapping = {
        tablePerHierarchy false
        table name: "SQL_TEMPLT_VALUE"
        field column: "FIELD"
    }
}
