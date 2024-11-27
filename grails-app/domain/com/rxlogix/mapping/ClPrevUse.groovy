package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClPrevUse implements SelectableList {

    BigDecimal id
    String description

    static mapping = {
        table "VW_CPRU_PREV_USE_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        description column: "PREV_USE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        description(maxSize: 25)
    }

    @Override
    def getSelectableList() {
        ClPrevUse.withTransaction {
            return ClPrevUse.findAll().unique().collect { it.description }.sort()
        }
    }
}
