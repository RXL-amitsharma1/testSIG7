package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmUdfDdlValues implements SelectableList {

    BigDecimal id
    String description

    static mapping = {
        datasource "pva"
        table "VW_LUDV_DESC_DSP"

        cache: "read-only"
        version false

        id column: "ID", type: "big_decimal", generator: "assigned"
        description column: "DESCRIPTION"
    }

    static constraints = {
        id(nullable:false, unique:true)
        description(blank:false, maxSize:100)

    }

    @Override
    def getSelectableList() {
        LmUdfDdlValues.withTransaction {
            return LmUdfDdlValues.findAll().unique().collect { it.description }.sort()
        }
    }
}
