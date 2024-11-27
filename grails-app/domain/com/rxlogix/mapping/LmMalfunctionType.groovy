package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmMalfunctionType implements SelectableList {

    BigDecimal id
    String name

    static mapping = {
        table "VW_LMFT_MALFUNCTION_TYPE_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "MALFUNCTION_TYPE_ID", type: "big_decimal", generator: "assigned"
        name column: "MALFUNCTION_TYPE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(maxSize: 100)
    }

    @Override
    def getSelectableList() {
        LmMalfunctionType.withTransaction {
            return LmMalfunctionType.findAll().unique().collect { it.name }.sort()
        }
    }
}