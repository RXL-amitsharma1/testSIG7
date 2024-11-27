package com.rxlogix.config

class ExecutedQueryExpressionValue extends QueryExpressionValue {
    static auditable = false

    static mapping = {
        table name: "EX_QUERY_EXP"
    }

    static constraints = {
        value(nullable: true)
    }

}
