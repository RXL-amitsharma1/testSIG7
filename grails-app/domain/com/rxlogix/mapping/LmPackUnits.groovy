package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmPackUnits implements SelectableList {

    BigDecimal id
    String description

    static mapping = {
        table "VW_PACK_UNITS_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        description column: "DESCRIPTION"
    }

    static constraints = {
        id(nullable:false, unique:true)
        description(nullable:false, maxSize: 20)
    }

    @Override
    def getSelectableList() {
        LmPackUnits.withTransaction {
            return LmPackUnits.findAll().unique().collect { it.description }.sort()
        }
    }
}
