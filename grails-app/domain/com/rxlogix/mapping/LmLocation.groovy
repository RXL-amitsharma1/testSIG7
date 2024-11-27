package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmLocation implements SelectableList {

    BigDecimal id
    String name

    static mapping = {
        table "VW_LLOC_LOCATION_DESC_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "LOCATION_ID", type: "big_decimal", generator: "assigned"
        name column: "LOCATION_DESC"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(maxSize: 20)
    }

    @Override
    def getSelectableList() {
        LmLocation.withTransaction {
            return LmLocation.findAll().unique().collect { it.name }.sort()
        }
    }
}
