package com.rxlogix.config

class ExecutedCustomSQLValue extends CustomSQLValue {
    static auditable = false

    static mapping = {
        table name: "EX_SQL_VALUE"
    }

}
