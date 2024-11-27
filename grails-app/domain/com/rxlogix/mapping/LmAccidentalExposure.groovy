package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmAccidentalExposure implements SelectableList {

    BigDecimal id
    String description

    static mapping = {
        table "VW_LAE_DESC_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "id", type: "big_decimal", generator: "assigned"
        description column: "DESCRIPTION"
    }

    static constraints = {
        id(nullable:false, unique:true)
        description(maxSize: 20)
    }

    @Override
    def getSelectableList() {
        LmAccidentalExposure.withTransaction {
            return LmAccidentalExposure.findAll().unique().collect { it.description }.sort()
        }
    }
}
