package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClTermType implements SelectableList {

    BigDecimal id
    String termType

    static mapping = {
        table "VW_CTT_TERM_TYPE"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        termType column: "TERM_TYPE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        termType(maxSize: 100)
    }

    @Override
    def getSelectableList() {
        ClTermType.withTransaction {
            return ClTermType.findAll().unique().collect { it.termType }.sort()
        }
    }
}
