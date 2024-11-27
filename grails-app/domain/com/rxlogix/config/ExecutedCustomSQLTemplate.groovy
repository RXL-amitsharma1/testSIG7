package com.rxlogix.config

class ExecutedCustomSQLTemplate extends CustomSQLTemplate {
    static auditable = false

    static mapping = {
        table name: "EX_CUSTOM_SQL_TEMPLT"
    }

    static constraints = {
        originalTemplateId(nullable:false,min:1)
    }
}
