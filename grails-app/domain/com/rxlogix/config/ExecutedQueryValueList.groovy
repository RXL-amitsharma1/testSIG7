package com.rxlogix.config

class ExecutedQueryValueList extends QueryValueList {
    static auditable = false

    static mapping = {
        table name: "EX_QUERY_VALUE"
    }

}
