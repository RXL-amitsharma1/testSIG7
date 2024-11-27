package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmIntermediary implements SelectableList {

    BigDecimal id
    String intermediary

    static mapping = {
        datasource "pva"
        table "VW_LINT_INTERMEDIARY_DSP"

        cache: "read-only"
        version false

        id column: "INTERMEDIARY_ID", type: "big_decimal", generator: "assigned"
        intermediary column: "INTERMEDIARY"
    }

    static constraints = {
        id(nullable:false, unique:true)
        intermediary(blank:false, maxSize:30)

    }

    @Override
    def getSelectableList() {
        LmIntermediary.withTransaction {
            return LmIntermediary.findAll().unique().collect { it.intermediary }.sort()
        }
    }
}
