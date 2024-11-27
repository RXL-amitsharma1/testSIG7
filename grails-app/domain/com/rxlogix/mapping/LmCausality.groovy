package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmCausality implements SelectableList {

    BigDecimal id
    String causality

    static mapping = {
        datasource "pva"
        table "VW_PUD_CAUSALITY"

        cache: "read-only"
        version false

        id column: "ID", type: "big_decimal", generator: "assigned"
        causality column: "CAUSALITY"
    }

    static constraints = {
        id(nullable:false, unique:true)
        causality(blank:false, maxSize:35)

    }

    @Override
    def getSelectableList() {
        LmCausality.withTransaction {
            return LmCausality.findAll().unique().collect { it.causality }.sort()
        }
    }
}
