package com.rxlogix.config

class ExecutedNonCaseSQLTemplate extends NonCaseSQLTemplate{
    static auditable = false

    static mapping = {
        table name: "EX_NCASE_SQL_TEMPLT"

        originalTemplateId column: "ORIG_TEMPLT_ID"
    }

    static constraints = {
        originalTemplateId(nullable:false,min:1)
    }
}
